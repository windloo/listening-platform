import { ElMessage } from 'element-plus'
import { BizError } from '@windloo/shared'
export function notifyError(e: unknown): void {
  ElMessage.error(e instanceof BizError ? e.message : '操作失败')
}
export function notifySuccess(msg = '操作成功'): void { ElMessage.success(msg) }
