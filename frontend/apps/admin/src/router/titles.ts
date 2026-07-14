export const routeTitles: Record<string, { title: string; parent?: string }> = {
  dashboard: { title: '首页' },
  users: { title: '用户管理' },
  categories: { title: '分类', parent: '内容管理' },
  albums: { title: '专辑', parent: '内容管理' },
  episodes: { title: '单集', parent: '内容管理' },
  profile: { title: '个人信息' },
}