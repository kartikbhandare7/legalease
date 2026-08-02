import { createSlice } from '@reduxjs/toolkit'
// In authSlice.js — add this thunk:
import { createAsyncThunk } from '@reduxjs/toolkit'
import api from '@/api/axiosInstance'

export const refreshCurrentUser = createAsyncThunk(
  'auth/refreshCurrentUser',
  async (_, { rejectWithValue }) => {
    try {
      const res = await api.get('/api/auth/me')
      return res.data
    } catch {
      return rejectWithValue(null)
    }
  }
)

const token = localStorage.getItem('token')
const user  = JSON.parse(localStorage.getItem('user') || 'null')

const authSlice = createSlice({
  name: 'auth',
  initialState: {
    token:           token || null,
    user:            user  || null,
    isAuthenticated: !!token,
  },
  reducers: {
    setCredentials: (state, action) => {
      const { token, ...user } = action.payload
      state.token           = token
      state.user            = user
      state.isAuthenticated = true
      localStorage.setItem('token', token)
      localStorage.setItem('user', JSON.stringify(user))
    },
    logout: state => {
      state.token           = null
      state.user            = null
      state.isAuthenticated = false
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    },
    extraReducers: (builder) => {
    builder
      .addCase(refreshCurrentUser.fulfilled, (state, action) => {
        if (action.payload) {
          state.user = {
            ...state.user,
            ...action.payload,
          }

          localStorage.setItem(
            'user',
            JSON.stringify(state.user)
          )
        }
      })

      .addCase(refreshCurrentUser.rejected, (state) => {
        state.token = null
        state.user = null
        state.isAuthenticated = false

        localStorage.removeItem('token')
        localStorage.removeItem('user')
      })
  }
  }
})

export const { setCredentials, logout } = authSlice.actions
export default authSlice.reducer