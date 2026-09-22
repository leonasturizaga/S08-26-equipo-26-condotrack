import { useCallback, useEffect, useState } from 'react'
import { useTranslation } from '../i18n/i18n.js'
import { useAuth } from '../auth/AuthContext.jsx'
import Modal from '../components/Modal.jsx'
import { createMaintenance, getAssignableMaintenanceStaff, getMaintenance, getMaintenanceRequest, getMaintenanceUnitOptions, updateMaintenance, updateMaintenanceAssignment, updateMaintenanceStatus } from '../api/maintenanceApi.js'

const PAGE_SIZE = 20
const PRIORITIES = ['LOW', 'MEDIUM', 'HIGH', 'URGENT']
const STATUSES = ['CREATED', 'ASSIGNED', 'IN_PROGRESS', 'ON_HOLD', 'RESOLVED', 'CLOSED', 'CANCELLED']

function formatDateTime(value) {
  if (!value) return '—'
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? value : new Intl.DateTimeFormat(undefined, { dateStyle: 'short', timeStyle: 'short' }).format(date)
}
function toLocal(value) {
  if (!value) return ''
  const d = new Date(value); if (Number.isNaN(d.getTime())) return ''
  const offset = d.getTimezoneOffset() * 60000
  return new Date(d.getTime() - offset).toISOString().slice(0,16)
}
function fromLocal(value) { return value ? new Date(value).toISOString() : null }
function statusClass(status) {
  if (status === 'CREATED' || status === 'ON_HOLD') return 'status-warning'
  if (status === 'ASSIGNED' || status === 'IN_PROGRESS') return 'status-info'
  if (status === 'RESOLVED' || status === 'CLOSED') return 'status-success'
  if (status === 'CANCELLED') return 'status-danger'
  return 'status-muted'
}
function priorityClass(priority) {
  if (priority === 'URGENT' || priority === 'HIGH') return 'status-danger'
  if (priority === 'MEDIUM') return 'status-warning'
  if (priority === 'LOW') return 'status-success'
  return 'status-muted'
}
const createDefault = () => ({ unitId: '', incidentId: '', priority: 'MEDIUM', category: '', description: '', scheduledAt: '' })
const editDefault = () => ({ priority: 'MEDIUM', category: '', description: '', scheduledAt: '' })

function MaintenanceForm({ form, onChange, unitOptions, saving, error, onSubmit, onCancel, t, showUnit = true }) {
  return <form className="entity-form" onSubmit={onSubmit}>
    {error && <div className="modal-feedback feedback feedback-error" role="alert">{error}</div>}
    {showUnit && <label className="form-field"><span>{t('Unit')}</span><select name="unitId" value={form.unitId} onChange={onChange} disabled={saving} required><option value="">{t('Select unit')}</option>{unitOptions.map((unit)=><option key={unit.unitId} value={unit.unitId}>{unit.buildingCode} · {unit.unitNumber}</option>)}</select></label>}
    <div className="form-grid-2">
      <label className="form-field"><span>{t('Priority')}</span><select name="priority" value={form.priority} onChange={onChange} disabled={saving}>{PRIORITIES.map(p=><option key={p} value={p}>{t(p)}</option>)}</select></label>
      <label className="form-field"><span>{t('Scheduled at')}</span><input type="datetime-local" name="scheduledAt" value={form.scheduledAt} onChange={onChange} disabled={saving}/></label>
    </div>
    <label className="form-field"><span>{t('Category')}</span><input name="category" value={form.category} onChange={onChange} maxLength={60} disabled={saving} required/></label>
    <label className="form-field"><span>{t('Description')}</span><textarea name="description" value={form.description} onChange={onChange} rows={6} maxLength={10000} disabled={saving} required/></label>
    <div className="entity-form-actions"><button className="button button-secondary" type="button" onClick={onCancel} disabled={saving}>{t('Cancel')}</button><button className="button button-primary" type="submit" disabled={saving}>{saving ? t('Saving...') : t('Save')}</button></div>
  </form>
}

function MaintenancePage() {
  const { t } = useTranslation(); const { user } = useAuth(); const role = user?.roles?.[0] || user?.role
  const canCreate = ['ADMINISTRATOR','RECEPTION','RESIDENT'].includes(role)
  const canManage = role === 'ADMINISTRATOR'; const canProviderUpdate = role === 'PROVIDER'
  const canList = ['ADMINISTRATOR','RESIDENT','OWNER','PROVIDER'].includes(role); const canAccess = canCreate || canList
  const [items,setItems]=useState([]); const [unitOptions,setUnitOptions]=useState([]); const [staffOptions,setStaffOptions]=useState([])
  const [page,setPage]=useState(0); const [totalPages,setTotalPages]=useState(0); const [totalElements,setTotalElements]=useState(0)
  const [loading,setLoading]=useState(false); const [error,setError]=useState(''); const [optionsError,setOptionsError]=useState(''); const [success,setSuccess]=useState('')
  const [showCreate,setShowCreate]=useState(false); const [createForm,setCreateForm]=useState(createDefault()); const [formError,setFormError]=useState(''); const [saving,setSaving]=useState(false)
  const [selected,setSelected]=useState(null); const [detailLoading,setDetailLoading]=useState(false); const [detailError,setDetailError]=useState('')
  const [editMode,setEditMode]=useState(false); const [editForm,setEditForm]=useState(editDefault()); const [assignedId,setAssignedId]=useState(''); const [statusTarget,setStatusTarget]=useState(''); const [resolution,setResolution]=useState(''); const [actionLoading,setActionLoading]=useState(false); const [staffLoading,setStaffLoading]=useState(false)

  const load = useCallback(async (target=0)=>{ if(!canList) return; setLoading(true); setError(''); try { const r=await getMaintenance(target,PAGE_SIZE); setItems(Array.isArray(r?.content)?r.content:[]); setPage(r?.page??target); setTotalPages(r?.totalPages??0); setTotalElements(r?.totalElements??0) } catch(e){setError(e.message||t('Unable to load maintenance.'));setItems([])} finally{setLoading(false)} },[canList,t])
  const loadUnits = useCallback(async()=>{ if(!canCreate)return; setOptionsError(''); try{const r=await getMaintenanceUnitOptions();setUnitOptions(Array.isArray(r)?r:[])}catch(e){setOptionsError(e.message||t('Unable to load maintenance units.'))}},[canCreate,t])
  useEffect(()=>{ if(canList) load(0) },[canList,load])
  useEffect(()=>{ loadUnits() },[loadUnits])
  const handleCreateChange=e=>setCreateForm(c=>({...c,[e.target.name]:e.target.value}))
  const handleEditChange=e=>setEditForm(c=>({...c,[e.target.name]:e.target.value}))
  const resetCreate=()=>{setCreateForm(createDefault());setFormError('')}
  const openCreate=()=>{resetCreate();setSuccess('');setShowCreate(true)}
  const submitCreate=async e=>{e.preventDefault();setSaving(true);setFormError('');try{await createMaintenance({unitId:createForm.unitId,incidentId:createForm.incidentId||null,priority:createForm.priority,category:createForm.category.trim(),description:createForm.description.trim(),scheduledAt:fromLocal(createForm.scheduledAt)});setShowCreate(false);resetCreate();setSuccess(t('Maintenance request created successfully.'));if(canList)await load(0)}catch(err){setFormError(err.message||t('Unable to create maintenance request.'))}finally{setSaving(false)}}
  const openItem=async id=>{setSelected(null);setDetailLoading(true);setDetailError('');setEditMode(false);setStatusTarget('');setResolution('');setStaffOptions([]);setAssignedId('');try{const r=await getMaintenanceRequest(id);if(!r||typeof r!=='object')throw new Error(t('The maintenance details could not be loaded.'));setSelected(r);setEditForm({priority:r.priority||'MEDIUM',category:r.category||'',description:r.description||'',scheduledAt:toLocal(r.scheduledAt)});setAssignedId(r.assignedToStaffId||'');if(canManage){setStaffLoading(true);try{const staff=await getAssignableMaintenanceStaff(r.buildingId);setStaffOptions(Array.isArray(staff)?staff:[])}catch(e){setDetailError(e.message||t('Unable to load maintenance staff.'))}finally{setStaffLoading(false)}}}catch(e){setDetailError(e.message||t('Unable to load maintenance request.'))}finally{setDetailLoading(false)}}
  const saveEdit=async()=>{if(!selected)return;setActionLoading(true);setDetailError('');try{const r=await updateMaintenance(selected.id,{priority:editForm.priority,category:editForm.category.trim(),description:editForm.description.trim(),scheduledAt:fromLocal(editForm.scheduledAt)});setSelected(r);setEditMode(false);setSuccess(t('Maintenance request updated successfully.'));if(canList)await load(page)}catch(e){setDetailError(e.message||t('Unable to update maintenance request.'))}finally{setActionLoading(false)}}
  const saveAssignment=async()=>{if(!selected)return;setActionLoading(true);setDetailError('');try{const r=await updateMaintenanceAssignment(selected.id,assignedId||null);setSelected(r);setSuccess(t('Maintenance assignment updated successfully.'));if(canList)await load(page)}catch(e){setDetailError(e.message||t('Unable to update maintenance assignment.'))}finally{setActionLoading(false)}}
  const saveStatus=async()=>{if(!selected||!statusTarget)return;setActionLoading(true);setDetailError('');try{const r=await updateMaintenanceStatus(selected.id,statusTarget,resolution||null);setSelected(r);setStatusTarget('');setResolution('');setSuccess(t('Maintenance status updated successfully.'));if(canList)await load(page)}catch(e){setDetailError(e.message||t('Unable to update maintenance status.'))}finally{setActionLoading(false)}}
  const canClose=!!selected && !actionLoading
  if(!canAccess) return <section className="page-shell"><div className="empty-state">{t('You do not have access to this module.')}</div></section>
  return <section className="page-shell">
    <div className="page-header"><div><h1>{t('Maintenance')}</h1><p>{t('Track maintenance requests, assignments and resolution.')}</p></div>{canCreate&&<button className="button button-primary" type="button" onClick={openCreate}>{t('Create maintenance request')}</button>}</div>
    {success&&<div className="feedback feedback-success" role="status">{success}</div>}{optionsError&&<div className="feedback feedback-error" role="alert">{optionsError}</div>}
    {!canList ? <div className="panel"><div className="empty-state">{t('Maintenance requests you create will appear here when list access is available.')}</div></div> : <div className="panel">
      <div className="panel-header"><div><h2>{t('Maintenance requests')}</h2><span className="panel-meta">{totalElements} {t('records')}</span></div></div>
      {error&&<div className="feedback feedback-error" role="alert">{error}</div>}
      {loading?<div className="feedback feedback-info">{t('Loading maintenance...')}</div>:items.length===0?<div className="empty-state">{t('No maintenance requests found.')}</div>:<div className="table-wrap"><table className="data-table"><thead><tr><th>{t('Building')}</th><th>{t('Unit')}</th><th>{t('Category')}</th><th>{t('Priority')}</th><th>{t('Status')}</th><th>{t('Scheduled at')}</th><th>{t('Actions')}</th></tr></thead><tbody>{items.map(item=><tr key={item.id}><td>{item.buildingCode}</td><td>{item.unitNumber}</td><td>{item.category}</td><td><span className={`status-pill ${priorityClass(item.priority)}`}>{t(item.priority)}</span></td><td><span className={`status-pill ${statusClass(item.status)}`}>{t(item.status)}</span></td><td>{formatDateTime(item.scheduledAt)}</td><td><button className="button button-ghost button-small" type="button" onClick={()=>openItem(item.id)}>{t('View')}</button></td></tr>)}</tbody></table></div>}
      {totalPages>1&&<div className="pagination"><button className="button button-secondary button-small" type="button" onClick={()=>load(Math.max(page-1,0))} disabled={page===0||loading}>{t('Previous')}</button><span>{t('Page')} {page+1} {t('of')} {totalPages}</span><button className="button button-secondary button-small" type="button" onClick={()=>load(Math.min(page+1,totalPages-1))} disabled={page>=totalPages-1||loading}>{t('Next')}</button></div>}
    </div>}

    <Modal open={showCreate} title={t('Create maintenance request')} onClose={()=>!saving&&setShowCreate(false)}>
      <MaintenanceForm form={createForm} onChange={handleCreateChange} unitOptions={unitOptions} saving={saving} error={formError} onSubmit={submitCreate} onCancel={()=>!saving&&setShowCreate(false)} t={t}/>
    </Modal>

    <Modal open={detailLoading||!!selected} title={t('Maintenance request')} onClose={()=>canClose&&setSelected(null)}>
      {detailLoading?<div className="feedback feedback-info">{t('Loading...')}</div>:detailError&&!selected?<div className="feedback feedback-error" role="alert">{detailError}</div>:selected?<div className="detail-layout">
        {detailError&&<div className="feedback feedback-error" role="alert">{detailError}</div>}
        <div className="detail-grid"><div><span>{t('Building')}</span><strong>{selected.buildingCode}</strong></div><div><span>{t('Unit')}</span><strong>{selected.unitNumber}</strong></div><div><span>{t('Category')}</span><strong>{selected.category}</strong></div><div><span>{t('Priority')}</span><strong><span className={`status-pill ${priorityClass(selected.priority)}`}>{t(selected.priority)}</span></strong></div><div><span>{t('Status')}</span><strong><span className={`status-pill ${statusClass(selected.status)}`}>{t(selected.status)}</span></strong></div><div><span>{t('Requested by')}</span><strong>{selected.requestedByName||'—'}</strong></div></div>
        <div className="detail-section"><span>{t('Description')}</span><p>{selected.description}</p></div>
        <div className="detail-grid"><div><span>{t('Created')}</span><strong>{formatDateTime(selected.createdAt)}</strong></div><div><span>{t('Scheduled at')}</span><strong>{formatDateTime(selected.scheduledAt)}</strong></div><div><span>{t('Assigned to')}</span><strong>{selected.assignedToName||t('Unassigned')}</strong></div><div><span>{t('Completed at')}</span><strong>{formatDateTime(selected.completedAt)}</strong></div></div>
        {selected.resolution&&<div className="detail-section"><span>{t('Resolution')}</span><p>{selected.resolution}</p></div>}

        {canManage&&<div className="detail-actions-section"><h3>{t('Administrator actions')}</h3>{!editMode?<button className="button button-secondary" type="button" onClick={()=>setEditMode(true)} disabled={actionLoading}>{t('Edit')}</button>:<div><MaintenanceForm form={editForm} onChange={handleEditChange} unitOptions={[]} showUnit={false} saving={actionLoading} error="" onSubmit={e=>{e.preventDefault();saveEdit()}} onCancel={()=>setEditMode(false)} t={t}/></div>}
          <label className="form-field"><span>{t('Assigned maintenance staff')}</span><select value={assignedId} onChange={e=>setAssignedId(e.target.value)} disabled={actionLoading||staffLoading}><option value="">{t('Unassigned')}</option>{staffOptions.map(s=><option key={s.staffId} value={s.staffId}>{s.firstName} {s.lastName} · {s.employeeCode||s.staffType}</option>)}</select></label><button className="button button-primary button-small" type="button" onClick={saveAssignment} disabled={actionLoading||staffLoading}>{t('Save assignment')}</button>
        </div>}

        {(canManage||canProviderUpdate)&&selected.status!=='CLOSED'&&selected.status!=='CANCELLED'&&<div className="detail-actions-section"><h3>{t('Status')}</h3><div className="form-grid-2"><label className="form-field"><span>{t('Next status')}</span><select value={statusTarget} onChange={e=>setStatusTarget(e.target.value)} disabled={actionLoading}><option value="">{t('Select status')}</option>{STATUSES.filter(s=>s!==selected.status).map(s=><option key={s} value={s}>{t(s)}</option>)}</select></label><label className="form-field"><span>{t('Resolution')}</span><textarea rows={3} value={resolution} onChange={e=>setResolution(e.target.value)} disabled={actionLoading}/></label></div><button className="button button-primary button-small" type="button" onClick={saveStatus} disabled={actionLoading||!statusTarget}>{t('Update status')}</button></div>}
      </div>:null}
    </Modal>
  </section>
}
export default MaintenancePage
