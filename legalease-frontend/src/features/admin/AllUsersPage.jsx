import { useState } from 'react'
import {
  useGetAllUsersQuery,
  useDeleteUserMutation
} from './adminApi'
import Table from '@/components/common/Table'
import Badge from '@/components/common/Badge'
import Button from '@/components/common/Button'
import Pagination from '@/components/common/Pagination'
import ConfirmDialog from '@/components/common/ConfirmDialog'
import { formatDate } from '@/utils/formatDate'
import { Trash2, Filter } from 'lucide-react'
import toast from 'react-hot-toast'

const ROLE_FILTERS = [
  { label: 'All',    value: ''            },
  { label: 'Lawyers', value: 'ROLE_LAWYER' },
  { label: 'Clerks',  value: 'ROLE_CLERK'  },
  { label: 'Admins',  value: 'ROLE_ADMIN'  },
]

export default function AllUsersPage() {
  const [page,     setPage]     = useState(0)
  const [role,     setRole]     = useState('')
  const [deleteId, setDeleteId] = useState(null)

  const { data, isLoading } = useGetAllUsersQuery({
    page, size: 10, role: role || undefined
  })
  const [deleteUser, { isLoading: deleting }] = useDeleteUserMutation()

  async function handleDelete() {
    try {
      await deleteUser(deleteId).unwrap()
      toast.success('User deleted')
      setDeleteId(null)
    } catch (err) {
      toast.error(err?.data?.error ?? 'Delete failed')
    }
  }

  const columns = [
    {
      key: 'fullName',
      label: 'User',
      render: row => (
        <div className="flex items-center gap-3">
          <div className="w-7 h-7 rounded-full bg-accent/10 flex items-center
                           justify-center text-accent text-xs font-semibold
                           font-mono shrink-0">
            {row.fullName.charAt(0)}
          </div>
          <div>
            <p className="font-semibold text-sm text-ink font-body">
              {row.fullName}
            </p>
            <p className="text-xs text-text-muted font-body">{row.email}</p>
          </div>
        </div>
      )
    },
    {
      key: 'role',
      label: 'Role',
      render: row => <Badge value={row.role?.replace('ROLE_', '')} />
    },
    {
      key: 'accountStatus',
      label: 'Status',
      render: row => <Badge value={row.accountStatus} />
    },
    {
      key: 'authProvider',
      label: 'Auth',
      render: row => (
        <span className="text-xs font-mono text-text-muted">
          {row.authProvider}
        </span>
      )
    },
    {
      key: 'createdAt',
      label: 'Joined',
      render: row => (
        <span className="font-mono text-xs text-text-muted">
          {formatDate(row.createdAt)}
        </span>
      )
    },
    {
      key: 'actions',
      label: '',
      render: row => (
        <div className="flex justify-end">
          {row.role !== 'ROLE_ADMIN' && (
            <button
              onClick={() => setDeleteId(row.id)}
              className="p-1.5 rounded hover:bg-danger/10 text-text-muted
                          hover:text-danger transition-colors"
              title="Delete user"
            >
              <Trash2 size={14} />
            </button>
          )}
        </div>
      )
    }
  ]

  return (
    <div className="max-w-6xl space-y-6">

      <div className="flex items-center justify-between">
        <div>
          <h2 className="font-display text-xl font-semibold text-ink">
            All users
          </h2>
          <p className="text-sm text-text-muted font-body mt-0.5">
            {data?.totalElements ?? 0} registered users
          </p>
        </div>
      </div>

      {/* Role filter */}
      <div className="flex items-center gap-1 border border-border rounded
                       bg-surface-alt p-0.5 w-fit">
        <Filter size={12} className="text-text-muted ml-2" />
        {ROLE_FILTERS.map(f => (
          <button
            key={f.value}
            onClick={() => { setRole(f.value); setPage(0) }}
            className={`
              px-3 py-1 text-xs font-semibold font-body rounded
              transition-all duration-150
              ${role === f.value
                ? 'bg-ink text-white'
                : 'text-text-muted hover:text-text'
              }
            `}
          >
            {f.label}
          </button>
        ))}
      </div>

      <Table
        columns={columns}
        data={data?.content}
        loading={isLoading}
        emptyTitle="No users found"
      />

      <Pagination
        page={page}
        totalPages={data?.totalPages ?? 0}
        onPageChange={setPage}
      />

      <ConfirmDialog
        open={!!deleteId}
        onClose={() => setDeleteId(null)}
        onConfirm={handleDelete}
        loading={deleting}
        title="Delete user"
        description="This permanently deletes the user account and all their data."
        confirmLabel="Delete user"
      />
    </div>
  )
}