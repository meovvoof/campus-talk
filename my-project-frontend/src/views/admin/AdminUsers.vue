<script setup>
import { get, post } from '@/net'
import { ElMessage } from 'element-plus'
import { onMounted, reactive, ref } from 'vue'
import { Refresh, Search } from '@element-plus/icons-vue'

const loading = ref(false)
const users = ref([])
const total = ref(0)
const query = reactive({
  page: 1,
  size: 10,
  keyword: ''
})

function formatTime(time) {
  return time ? new Date(time).toLocaleString() : ''
}

function loadUsers() {
  loading.value = true
  const keyword = query.keyword ? `&keyword=${encodeURIComponent(query.keyword)}` : ''
  get(`/api/admin/user/list?page=${query.page}&size=${query.size}${keyword}`, data => {
    users.value = data.list || []
    total.value = data.total || 0
    loading.value = false
  }, () => {
    users.value = []
    total.value = 0
    loading.value = false
  })
}

function saveStatus(row) {
  get(`/api/admin/user/detail?id=${row.id}`, data => {
    post('/api/admin/user/save', {
      ...row,
      detail: data.detail,
      privacy: data.privacy
    }, () => {
      ElMessage.success('用户状态已更新')
    }, message => {
      ElMessage.warning(message)
      loadUsers()
    })
  })
}

onMounted(loadUsers)
</script>

<template>
  <div class="admin-page">
    <section class="admin-panel">
      <div class="page-head">
        <div>
          <h2>用户管理</h2>
          <span>查询用户并维护禁言、封禁状态</span>
        </div>
        <el-button :icon="Refresh" @click="loadUsers">刷新</el-button>
      </div>

      <div class="toolbar">
        <el-input
            v-model="query.keyword"
            clearable
            placeholder="输入用户 ID 或用户名"
            style="max-width: 320px"
            @keyup.enter="loadUsers">
          <template #prefix>
            <el-icon><Search/></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="loadUsers">搜索</el-button>
      </div>

      <el-table v-loading="loading" :data="users" border>
        <el-table-column prop="id" label="ID" width="90"/>
        <el-table-column prop="username" label="用户名" min-width="130"/>
        <el-table-column prop="email" label="邮箱" min-width="190"/>
        <el-table-column prop="role" label="角色" width="100"/>
        <el-table-column label="注册时间" min-width="170">
          <template #default="{ row }">{{ formatTime(row.registerTime) }}</template>
        </el-table-column>
        <el-table-column label="禁言" width="110">
          <template #default="{ row }">
            <el-switch v-model="row.mute" @change="saveStatus(row)"/>
          </template>
        </el-table-column>
        <el-table-column label="封禁" width="110">
          <template #default="{ row }">
            <el-switch v-model="row.banned" @change="saveStatus(row)"/>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
            layout="prev, pager, next, total"
            :page-size="query.size"
            :total="total"
            v-model:current-page="query.page"
            @current-change="loadUsers"/>
      </div>
    </section>
  </div>
</template>

<style scoped>
.admin-page {
  padding: 18px;
}

.admin-panel {
  max-width: 1100px;
  margin: 0 auto;
  padding: 18px;
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
}

.page-head,
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.page-head {
  margin-bottom: 16px;
}

.page-head h2 {
  margin: 0 0 4px;
}

.page-head span {
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.toolbar {
  justify-content: flex-start;
  margin-bottom: 14px;
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 14px;
}
</style>
