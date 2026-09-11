export const mockUsers = [
  {
    id: 'mock-admin-001',
    username: 'admin',
    email: 'admin@condotrack.test',
    password: 'admin123',
    name: 'Alex Administrator',
    role: 'ADMINISTRATOR',
  },
  {
    id: 'mock-reception-001',
    username: 'reception',
    email: 'reception@condotrack.test',
    password: 'reception123',
    name: 'Sam Reception',
    role: 'RECEPTION',
  },
  {
    id: 'mock-resident-001',
    username: 'resident',
    email: 'resident@condotrack.test',
    password: 'resident123',
    name: 'Taylor Resident',
    role: 'RESIDENT',
  },
  {
    id: 'mock-owner-001',
    username: 'owner',
    email: 'owner@condotrack.test',
    password: 'owner123',
    name: 'Jordan Owner',
    role: 'OWNER',
  },
  {
    id: 'mock-provider-001',
    username: 'provider',
    email: 'provider@condotrack.test',
    password: 'provider123',
    name: 'Casey Provider',
    role: 'PROVIDER',
  },
]

export function authenticateMockUser(identifier, password) {
  const normalizedIdentifier = identifier.trim().toLowerCase()

  const user = mockUsers.find(
    (candidate) =>
      candidate.username.toLowerCase() === normalizedIdentifier ||
      candidate.email.toLowerCase() === normalizedIdentifier
  )

  if (!user || user.password !== password) {
    return null
  }

  const { password: _password, ...safeUser } = user

  return safeUser
}