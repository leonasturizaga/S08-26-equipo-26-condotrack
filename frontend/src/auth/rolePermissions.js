// src/auth/rolePermissions.js

export const moduleDefinitions = [
  {
    key: 'buildings',
    icon: 'building',
    label: 'Buildings',
    path: '/dashboard/buildings',
  },
  {
    key: 'units',
    icon: 'home',
    label: 'Units',
    path: '/dashboard/units',
  },
  {
    key: 'residents',
    icon: 'users',
    label: 'Residents / Owners',
    path: '/dashboard/residents',
  },
  {
    key: 'access',
    icon: 'door',
    label: 'Access / Visitors',
    path: '/dashboard/access',
  },
  {
    key: 'deliveries',
    icon: 'package',
    label: 'Deliveries / Mail',
    path: '/dashboard/deliveries',
  },
  {
    key: 'bookings',
    icon: 'calendar',
    label: 'Common Area Bookings',
    path: '/dashboard/bookings',
  },
  {
    key: 'incidents',
    icon: 'alert',
    label: 'Incidents',
    path: '/dashboard/incidents',
  },
  {
    key: 'maintenance',
    icon: 'wrench',
    label: 'Maintenance',
    path: '/dashboard/maintenance',
  },
  {
    key: 'moves',
    icon: 'move',
    label: 'Move Requests',
    path: '/dashboard/moves',
  },
  {
    key: 'reports',
    icon: 'chart',
    label: 'Reports / KPIs',
    path: '/dashboard/reports',
  },
  {
    key: 'buildingConfig',
    icon: 'settings',
    label: 'Building Configuration',
    path: '/dashboard/building-config',
  },
  {
    key: 'userManagement',
    icon: 'user',
    label: 'User Management',
    path: '/dashboard/user-management',
  },
  {
    key: 'communications',
    icon: 'megaphone',
    label: 'Communications',
    path: '/dashboard/communications',
  },
]

export const roleLabels = {
  ADMINISTRATOR: 'Administrator',
  RECEPTION: 'Reception',
  RESIDENT: 'Resident',
  OWNER: 'Owner',
  PROVIDER: 'Provider',
}

export const rolePermissions = {
  ADMINISTRATOR: {
    buildings: 'full',
    units: 'full',
    residents: 'full',
    access: 'view',
    deliveries: 'view',
    bookings: 'full',
    incidents: 'manage',
    maintenance: 'manage',
    moves: 'approve',
    reports: 'full',
    buildingConfig: 'full',
    userManagement: 'full',
    communications: 'create',
    generalSettings: 'full',
  },

  RECEPTION: {
    buildings: 'none',
    units: 'view',
    residents: 'view',
    access: 'create',
    deliveries: 'create',
    bookings: 'view',
    incidents: 'create',
    maintenance: 'create',
    moves: 'view',
    reports: 'none',
    buildingConfig: 'none',
    userManagement: 'none',
    communications: 'none',
  },

  RESIDENT: {
    buildings: 'none',
    units: 'own',
    residents: 'own',
    access: 'own',
    deliveries: 'own',
    bookings: 'own',
    incidents: 'own',
    maintenance: 'own',
    moves: 'own',
    reports: 'none',
    buildingConfig: 'none',
    userManagement: 'none',
    communications: 'view',
  },

  OWNER: {
    buildings: 'none',
    units: 'own',
    residents: 'own',
    access: 'none',
    deliveries: 'none',
    bookings: 'none',
    incidents: 'view',
    maintenance: 'view',
    moves: 'approve',
    reports: 'none',
    buildingConfig: 'none',
    userManagement: 'none',
    communications: 'view',
  },

  PROVIDER: {
    buildings: 'none',
    units: 'none',
    residents: 'none',
    access: 'none',
    deliveries: 'none',
    bookings: 'none',
    incidents: 'assigned',
    maintenance: 'assigned',
    moves: 'none',
    reports: 'none',
    buildingConfig: 'none',
    userManagement: 'none',
    communications: 'none',
  },
}

export function getRolePermissions(role) {
  return rolePermissions[role] || {}
}

export function getVisibleModules(role) {
  const permissions = getRolePermissions(role)

  return moduleDefinitions.filter(
    (module) => permissions[module.key] && permissions[module.key] !== 'none'
  )
}

export function getModulePermission(role, moduleKey) {
  const permissions = getRolePermissions(role)

  return permissions[moduleKey] || 'none'
}