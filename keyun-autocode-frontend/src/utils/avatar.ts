/**
 * 头像处理工具函数
 */

// 默认头像链接
const DEFAULT_AVATAR = 'https://rba.kanostar.top/adapt'

/**
 * 获取用户头像，如果没有则返回默认头像
 * @param avatar 用户头像链接
 * @returns 头像链接
 */
export const getAvatarUrl = (avatar?: string | null): string => {
  return avatar && avatar.trim() ? avatar : DEFAULT_AVATAR
}

/**
 * 获取用户头像或默认头像
 * @param user 用户对象
 * @returns 头像链接
 */
export const getUserAvatar = (user?: { avatar?: string | null } | null): string => {
  return getAvatarUrl(user?.avatar)
}
