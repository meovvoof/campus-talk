<script setup>
import { get } from '@/net'
import { objectUrl } from '@/utils/delta'
import router from '@/router'
import { computed, onMounted, reactive, ref } from 'vue'
import { ChatDotSquare, EditPen, Pointer, Star, View } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const types = ref([])
const topics = ref([])
const topTopics = ref([])
const query = reactive({
  page: 0,
  type: 0
})

const currentTypeName = computed(() => {
  if (query.type === 0) return '全部帖子'
  return types.value.find(item => item.id === query.type)?.name || '帖子列表'
})

function formatTime(time) {
  return time ? new Date(time).toLocaleString() : ''
}

function typeName(type) {
  return types.value.find(item => item.id === type)?.name || `分类 ${type}`
}

function loadTypes() {
  get('/api/forum/types', data => {
    types.value = data || []
  })
}

function loadTopTopics() {
  get('/api/forum/top-topic', data => {
    topTopics.value = data || []
  })
}

function loadTopics() {
  loading.value = true
  get(`/api/forum/list-topic?page=${query.page}&type=${query.type}`, data => {
    topics.value = data || []
    loading.value = false
  }, message => {
    topics.value = []
    loading.value = false
    ElMessage.warning(message)
  })
}

function changeType(type) {
  query.type = type
  query.page = 0
  loadTopics()
}

function nextPage(offset) {
  query.page = Math.max(0, query.page + offset)
  loadTopics()
}

function openTopic(id) {
  router.push(`/index/topic-detail/${id}`)
}

onMounted(() => {
  loadTypes()
  loadTopTopics()
  loadTopics()
})
</script>

<template>
  <div class="forum-page">
    <section class="forum-main">
      <div class="page-toolbar">
        <div>
          <h2>{{ currentTypeName }}</h2>
          <span>浏览校园社区最新讨论</span>
        </div>
        <el-button type="primary" :icon="EditPen" @click="router.push('/index/topic-create')">发布帖子</el-button>
      </div>

      <div class="type-tabs">
        <el-button :type="query.type === 0 ? 'primary' : 'default'" @click="changeType(0)">全部</el-button>
        <el-button
            v-for="item in types"
            :key="item.id"
            :type="query.type === item.id ? 'primary' : 'default'"
            @click="changeType(item.id)">
          {{ item.name }}
        </el-button>
      </div>

      <div v-loading="loading" class="topic-list">
        <el-empty v-if="!loading && topics.length === 0" description="暂无帖子"/>
        <article v-for="topic in topics" :key="topic.id" class="topic-card" @click="openTopic(topic.id)">
          <div class="topic-head">
            <el-tag size="small" :type="topic.top ? 'danger' : 'info'">
              {{ topic.top ? '置顶' : typeName(topic.type) }}
            </el-tag>
            <el-tag v-if="topic.locked" size="small" type="warning">已锁定</el-tag>
            <span>{{ formatTime(topic.time) }}</span>
          </div>
          <h3>{{ topic.title }}</h3>
          <p>{{ topic.text || '暂无摘要' }}</p>
          <div v-if="topic.images && topic.images.length" class="topic-images">
            <el-image
                v-for="image in topic.images.slice(0, 3)"
                :key="image"
                :src="objectUrl(image)"
                fit="cover"/>
          </div>
          <div class="topic-foot">
            <span>{{ topic.username || '匿名用户' }}</span>
            <span><el-icon><Pointer/></el-icon>{{ topic.like }}</span>
            <span><el-icon><Star/></el-icon>{{ topic.collect }}</span>
          </div>
        </article>
      </div>

      <div class="pager">
        <el-button :disabled="query.page === 0" @click="nextPage(-1)">上一页</el-button>
        <span>第 {{ query.page + 1 }} 页</span>
        <el-button :disabled="topics.length === 0" @click="nextPage(1)">下一页</el-button>
      </div>
    </section>

    <aside class="forum-side">
      <div class="side-panel">
        <div class="panel-title">
          <el-icon><ChatDotSquare/></el-icon>
          置顶主题
        </div>
        <el-empty v-if="topTopics.length === 0" description="暂无置顶"/>
        <div
            v-for="item in topTopics"
            :key="item.id"
            class="top-topic"
            @click="openTopic(item.id)">
          <div>{{ item.title }}</div>
          <span>{{ formatTime(item.time) }}</span>
        </div>
      </div>
      <div class="side-panel">
        <div class="panel-title">
          <el-icon><View/></el-icon>
          快捷入口
        </div>
        <el-button plain style="width: 100%" @click="router.push('/index/collects')">我的收藏</el-button>
      </div>
    </aside>
  </div>
</template>

<style scoped>
.forum-page {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 280px;
  gap: 16px;
  max-width: 1180px;
  margin: 0 auto;
  padding: 18px;
}

.forum-main,
.side-panel,
.topic-card {
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
}

.forum-main {
  padding: 18px;
}

.page-toolbar,
.topic-head,
.topic-foot,
.pager,
.panel-title {
  display: flex;
  align-items: center;
}

.page-toolbar {
  justify-content: space-between;
  gap: 16px;
}

.page-toolbar h2 {
  margin: 0 0 4px;
  font-size: 22px;
}

.page-toolbar span,
.topic-head span,
.top-topic span {
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.type-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin: 18px 0;
}

.topic-list {
  min-height: 300px;
}

.topic-card {
  padding: 16px;
  margin-bottom: 12px;
  cursor: pointer;
  transition: border-color .2s, transform .2s;
}

.topic-card:hover {
  border-color: var(--el-color-primary);
  transform: translateY(-1px);
}

.topic-head {
  gap: 8px;
}

.topic-card h3 {
  margin: 10px 0 8px;
  font-size: 18px;
}

.topic-card p {
  margin: 0;
  color: var(--el-text-color-regular);
  line-height: 1.7;
  white-space: pre-wrap;
}

.topic-images {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  margin-top: 12px;
}

.topic-images .el-image {
  height: 110px;
  border-radius: 6px;
  overflow: hidden;
}

.topic-foot {
  justify-content: space-between;
  gap: 14px;
  margin-top: 14px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.topic-foot span {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.pager {
  justify-content: center;
  gap: 14px;
  margin-top: 14px;
}

.forum-side {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.side-panel {
  padding: 14px;
}

.panel-title {
  gap: 6px;
  font-weight: 700;
  margin-bottom: 12px;
}

.top-topic {
  padding: 10px 0;
  border-top: 1px solid var(--el-border-color-lighter);
  cursor: pointer;
}

.top-topic:hover div {
  color: var(--el-color-primary);
}

@media (max-width: 900px) {
  .forum-page {
    grid-template-columns: 1fr;
  }
}
</style>
