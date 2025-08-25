<template>
  <a-layout-header class="header">
    <a-row :wrap="false" align="middle">
      <!-- 左侧：Logo和标题 -->
      <a-col flex="280px">
        <RouterLink to="/">
          <div class="header-left">
            <img class="logo" src="@/assets/logo.png" alt="Logo" />
            <h1 class="site-title">可云 AutoCode</h1>
          </div>
        </RouterLink>
      </a-col>
      <!-- 中间：导航菜单 -->
      <a-col flex="auto">
        <a-menu
          v-model:selectedKeys="selectedKeys"
          mode="horizontal"
          :items="menuItems"
          @click="handleMenuClick"
        />
      </a-col>
      <!-- 右侧：用户操作区域 -->
      <a-col>
        <div class="user-login-status">
          <div v-if="loginUserStore.loginUser.id">
            <a-dropdown>
              <a-space>
                <a-avatar :src="getUserAvatar(loginUserStore.loginUser)" />
                {{ loginUserStore.loginUser.username ?? '无名' }}
              </a-space>
              <template #overlay>
                <a-menu>
                  <a-menu-item @click="doLogout">
                    <LogoutOutlined />
                    退出登录
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </div>
          <div v-else>
            <a-button type="primary" href="/user/login">登录</a-button>
          </div>
        </div>
      </a-col>
    </a-row>
  </a-layout-header>
</template>

<script setup lang="ts">
import { computed, h, ref } from 'vue'
import { useRouter } from 'vue-router'
import { type MenuProps, message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser.ts'
import { userLogout } from '@/api/userController.ts'
import { LogoutOutlined, HomeOutlined, AppstoreOutlined } from '@ant-design/icons-vue'
import { getUserAvatar } from '@/utils/avatar'

const loginUserStore = useLoginUserStore()
const router = useRouter()
// 当前选中菜单
const selectedKeys = ref<string[]>(['/'])
// 监听路由变化，更新当前选中菜单
router.afterEach((to) => {
  selectedKeys.value = [to.path]
})

// 菜单配置项
const originItems = [
  {
    key: '/',
    icon: () => h(HomeOutlined),
    label: '主页',
    title: '主页',
  },
  {
    key: '/my-works',
    icon: () => h(AppstoreOutlined),
    label: '我的作品',
    title: '我的作品',
  },
  {
    key: '/admin/userManage',
    label: '用户管理',
    title: '用户管理',
  },
  {
    key: '/admin/appManage',
    label: '应用管理',
    title: '应用管理',
  },
]

// 过滤菜单项
const filterMenus = (menus = [] as MenuProps['items']) => {
  return menus?.filter((menu) => {
    const menuKey = menu?.key as string
    if (menuKey?.startsWith('/admin')) {
      const loginUser = loginUserStore.loginUser
      if (!loginUser || (loginUser.role !== 'admin' && loginUser.role !== 'super_admin')) {
        return false
      }
    }
    return true
  })
}

// 展示在菜单的路由数组
const menuItems = computed<MenuProps['items']>(() => filterMenus(originItems))

// 处理菜单点击
const handleMenuClick: MenuProps['onClick'] = (e) => {
  const key = e.key as string
  selectedKeys.value = [key]
  // 跳转到对应页面
  if (key.startsWith('/')) {
    router.push(key)
  }
}

// 退出登录
const doLogout = async () => {
  const res = await userLogout()
  if (res.data.code === 0) {
    loginUserStore.setLoginUser({
      username: '未登录',
    })
    message.success('退出登录成功')
    await router.push('/user/login')
  } else {
    message.error('退出登录失败，' + res.data.message)
  }
}
</script>

<style scoped>
.header {
  background: #fff;
  padding: 0 24px;
  height: 64px;
  display: flex;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
  height: 100%;
  padding: 8px 0;
  transition: all 0.3s ease;
}

.header-left:hover {
  transform: translateY(-1px);
}

.logo {
  height: 40px;
  width: 40px;
  transition: all 0.3s ease;
  filter: drop-shadow(0 2px 8px rgba(24, 144, 255, 0.3));
  flex-shrink: 0;
}

.logo:hover {
  transform: scale(1.05);
  filter: drop-shadow(0 4px 12px rgba(24, 144, 255, 0.5));
}

.site-title {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  font-family: 'Arial Black', 'Microsoft YaHei', sans-serif;
  background: linear-gradient(135deg, #1890ff 0%, #722ed1 50%, #13c2c2 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  text-shadow: 0 0 10px rgba(24, 144, 255, 0.3);
  position: relative;
  letter-spacing: 1px;
  transition: all 0.3s ease;
  white-space: nowrap;
  overflow: visible;
  flex-shrink: 0;
  line-height: 1.2;
}

.site-title:hover {
  transform: scale(1.02);
  filter: brightness(1.1);
}

.site-title::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, #1890ff 0%, #722ed1 50%, #13c2c2 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
  z-index: -1;
  border-radius: 4px;
  filter: blur(8px);
}

.site-title:hover::before {
  opacity: 0.1;
}

/* 添加科技感的装饰线条 */
.site-title::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 0;
  height: 2px;
  background: linear-gradient(90deg, #1890ff, #722ed1, #13c2c2);
  transition: width 0.3s ease;
}

.header-left:hover .site-title::after {
  width: 100%;
}

.ant-menu-horizontal {
  border-bottom: none !important;
  height: 64px;
  line-height: 64px;
  flex: 1;
}

/* 确保用户操作区域样式正常并保持在右侧 */
.user-login-status {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  height: 64px;
  padding-left: 24px;
  padding-right: 20px;
  margin-left: auto;
}

/* 确保整个行布局正确 */
.ant-row {
  height: 64px;
  width: 100%;
  display: flex !important;
  align-items: center !important;
  justify-content: space-between !important;
}

/* 确保RouterLink不影响布局 */
.header-left a {
  text-decoration: none;
  color: inherit;
  display: flex;
  align-items: center;
  gap: 16px;
  height: 100%;
}

/* 确保导航菜单占用中间所有可用空间 */
.ant-menu {
  flex: 1;
  display: flex;
  justify-content: flex-start;
  border-bottom: none !important;
}

/* 修复中间导航菜单的布局 */
.ant-col[flex="auto"] {
  flex: 1;
  display: flex;
  justify-content: flex-start;
  padding: 0 24px;
}
</style>
