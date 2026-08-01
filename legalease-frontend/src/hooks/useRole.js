import { useAuth } from './useAuth'

export function useRole() {
    const {user} = useAuth()

    return {
        isLawyer : user?.role === 'ROLE_LAWYER',
        isClerk : user?.role === 'ROLE_CLERK',
        isAdmin : user?.role === 'ROLE_ADMIN',
        hasRole: (role) => user?.role === role,
    }
}