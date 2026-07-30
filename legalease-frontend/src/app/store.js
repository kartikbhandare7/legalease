import { configureStore } from '@reduxjs/toolkit'
import { setupListeners } from '@reduxjs/toolkit/query'
import authReducer from '@/features/auth/authSlice'
import { authApi } from '@/features/auth/authApi'
import { casesApi } from '@/features/cases/casesApi'
import { clientsApi } from '@/features/clients/clientsApi'
import { hearingsApi } from '@/features/hearings/hearingsApi'
import { dashboardApi } from '@/features/dashboard/dashboardApi'
import { aiApi } from '@/features/ai/aiApi'
import { documentsApi } from '@/features/documents/documentsApi'
import { adminApi } from '@/features/admin/adminApi'

export const store = configureStore({
  reducer: {
    auth: authReducer,
    [authApi.reducerPath]:      authApi.reducer,
    [casesApi.reducerPath]:     casesApi.reducer,
    [clientsApi.reducerPath]:   clientsApi.reducer,
    [hearingsApi.reducerPath]:  hearingsApi.reducer,
    [dashboardApi.reducerPath]: dashboardApi.reducer,
    [aiApi.reducerPath]:        aiApi.reducer,
    [documentsApi.reducerPath]: documentsApi.reducer,
    [adminApi.reducerPath]:     adminApi.reducer,
  },
  middleware: getDefaultMiddleware =>
    getDefaultMiddleware().concat(
      authApi.middleware,
      casesApi.middleware,
      clientsApi.middleware,
      hearingsApi.middleware,
      dashboardApi.middleware,
      aiApi.middleware,
      documentsApi.middleware,
      adminApi.middleware,
    )
})

setupListeners(store.dispatch)
export default store