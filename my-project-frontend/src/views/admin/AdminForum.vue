<script setup>
import { get, post } from '@/net'
import { ElMessage, ElMessageBox } from 'element-plus'
import { onMounted, reactive, ref } from 'vue'
import { Delete, Plus, Refresh, Select } from '@element-plus/icons-vue'

const activeTab = ref('topics')
const topicLoading = ref(false)
const topics = ref([])
const total = ref(0)
const query = reactive({
  page: 1,
  size: 10,
  keyword: ''
})

const types = ref([])
const typeForm = reactive({
  id: null,
  name: '',
  desc: '',
  color: '#409eff'
})

const prohibitedText = ref('')

function formatTime(time) {
  return time ? new Date(time).toLocaleString() : ''
}

function loadTopics() {
  topicLoading.value = true
  const keyword = query.keyword ? `&keyword=${encodeURIComponent(query.keyword)}` : ''
  get(`/api/admin/forum/list?page=${query.page}&size=${query.size}${keyword}`, data => {
    topics.value = data.list || []
    total.value = data.total || 0
    topicLoading.value = false
  }, () => {
    topics.value = []
    total.value = 0
    topicLoading.value = false
  })
}

function updateTopicFlag(row, field, url) {
  post(url, {
    tid: row.id,
    status: !!row[field]
  }, () => {
    ElMessage.success('帖子状态已更新')
  }, message => {
    ElMessage.warning(message)
    loadTopics()
  })
}

function deleteTopic(row) {
  ElMessageBox.confirm(`确认删除帖子「${row.title}」？`, '删除确认', { type: 'warning' })
      .then(() => {
        get(`/api/admin/forum/delete?tid=${row.id}`, () => {
          ElMessage.success('帖子已删除')
          loadTopics()
        })
      })
      .catch(() => {})
}

function loadTypes() {
  get('/api/forum/types', data => {
    types.value = data || []
  })
}

function editType(row) {
  typeForm.id = row.id
  typeForm.name = row.name
  typeForm.desc = row.desc
  typeForm.color = row.color
}

function resetTypeForm() {
  typeForm.id = null
  typeForm.name = ''
  typeForm.desc = ''
  typeForm.color = '#409eff'
}

function saveType() {
  if (!typeForm.name.trim()) {
    ElMessage.warning('请输入分类名称')
    return
  }
  const url = typeForm.id ? '/api/admin/forum/update-type' : '/api/admin/forum/create-type'
  post(url, {...typeForm}, () => {
    ElMessage.success('分类已保存')
    resetTypeForm()
    loadTypes()
  })
}

function deleteType(row) {
  ElMessageBox.confirm(`确认删除分类「${row.name}」？该分类下帖子也会被删除。`, '删除确认', { type: 'warning' })
      .then(() => {
        get(`/api/admin/forum/delete-type?tid=${row.id}`, () => {
          ElMessage.success('分类已删除')
          loadTypes()
          loadTopics()
        })
      })
      .catch(() => {})
}

function loadProhibitedWords() {
  get('/api/admin/forum/prohibited-list', data => {
    prohibitedText.value = (data || []).join('\n')
  })
}

function saveProhibitedWords() {
  const words = prohibitedText.value
      .split('\n')
      .map(item => item.trim())
      .filter(Boolean)
  post('/api/admin/forum/prohibited-save', words, () => {
    ElMessage.success('违禁词已保存')
  })
}

onMounted(() => {
  loadTopics()
  loadTypes()
  loadProhibitedWords()
})
</script>

<template>
  <div class="admin-page">
    <section class="admin-panel">
      <div class="page-head">
        <div>
          <h2>论坛管理</h2>
          <span>管理帖子状态、分类和内容审核词库</span>
        </div>
        <el-button :icon="Refresh" @click="loadTopics">刷新</el-button>
      </div>

      <el-tabs v-model="activeTab">
        <el-tab-pane name="topics" label="帖子管理">
          <div class="toolbar">
            <el-input v-model="query.keyword" clearable placeholder="搜索标题" style="max-width: 320px" @keyup.enter="loadTopics"/>
            <el-button type="primary" @click="loadTopics">搜索</el-button>
          </div>
          <el-table v-loading="topicLoading" :data="topics" border>
            <el-table-column prop="id" label="ID" width="90"/>
            <el-table-column prop="title" label="标题" min-width="220"/>
            <el-table-column prop="username" label="作者" width="130"/>
            <el-table-column label="时间" min-width="170">
              <template #default="{ row }">{{ formatTime(row.time) }}</template>
            </el-table-column>
            <el-table-column label="置顶" width="100">
              <template #default="{ row }">
                <el-switch v-model="row.top" :active-value="1" :inactive-value="0" @change="updateTopicFlag(row, 'top', '/api/admin/forum/top')"/>
              </template>
            </el-table-column>
            <el-table-column label="锁定" width="100">
              <template #default="{ row }">
                <el-switch v-model="row.locked" :active-value="1" :inactive-value="0" @change="updateTopicFlag(row, 'locked', '/api/admin/forum/locked')"/>
              </template>
            </el-table-column>
            <el-table-column label="隐藏" width="100">
              <template #default="{ row }">
                <el-switch v-model="row.invisible" :active-value="1" :inactive-value="0" @change="updateTopicFlag(row, 'invisible', '/api/admin/forum/invisible')"/>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button type="danger" text :icon="Delete" @click="deleteTopic(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pager">
            <el-pagination
                layout="prev, pager, next, total"
                :page-size="query.size"
                :total="total"
                v-model:current-page="query.page"
                @current-change="loadTopics"/>
          </div>
        </el-tab-pane>

        <el-tab-pane name="types" label="分类管理">
          <div class="type-layout">
            <el-table :data="types" border>
              <el-table-column prop="id" label="ID" width="80"/>
              <el-table-column prop="name" label="名称"/>
              <el-table-column prop="desc" label="描述"/>
              <el-table-column prop="color" label="颜色" width="120">
                <template #default="{ row }">
                  <el-tag :color="row.color" effect="dark">{{ row.color }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="160">
                <template #default="{ row }">
                  <el-button text @click="editType(row)">编辑</el-button>
                  <el-button text type="danger" @click="deleteType(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            <div class="type-form">
              <h3>{{ typeForm.id ? '编辑分类' : '新建分类' }}</h3>
              <el-input v-model="typeForm.name" placeholder="分类名称"/>
              <el-input v-model="typeForm.desc" placeholder="分类描述"/>
              <el-color-picker v-model="typeForm.color"/>
              <div>
                <el-button @click="resetTypeForm">清空</el-button>
                <el-button type="primary" :icon="Plus" @click="saveType">保存</el-button>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane name="prohibited" label="内容审核">
          <el-alert type="info" :closable="false" title="每行一个违禁词，保存后后端发布和回复流程会使用该词库检测。"/>
          <el-input
              v-model="prohibitedText"
              type="textarea"
              :rows="14"
              placeholder="每行输入一个违禁词"
              style="margin: 12px 0"/>
          <el-button type="primary" :icon="Select" @click="saveProhibitedWords">保存违禁词</el-button>
        </el-tab-pane>
      </el-tabs>
    </section>
  </div>
</template>

<style scoped>
.admin-page {
  padding: 18px;
}

.admin-panel {
  max-width: 1180px;
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

.type-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 280px;
  gap: 16px;
}

.type-form {
  display: flex;
  flex-direction: column;
  gap: 10px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  padding: 14px;
}

.type-form h3 {
  margin: 0;
}

@media (max-width: 900px) {
  .type-layout {
    grid-template-columns: 1fr;
  }
}
</style>
