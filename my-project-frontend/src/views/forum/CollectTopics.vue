<script setup>
import { get } from '@/net'
import router from '@/router'
import { objectUrl } from '@/utils/delta'
import { onMounted, ref } from 'vue'
import { Collection, Pointer, Star } from '@element-plus/icons-vue'

const loading = ref(false)
const topics = ref([])

function formatTime(time) {
  return time ? new Date(time).toLocaleString() : ''
}

function loadCollects() {
  loading.value = true
  get('/api/forum/collects', data => {
    topics.value = data || []
    loading.value = false
  }, () => {
    topics.value = []
    loading.value = false
  })
}

onMounted(loadCollects)
</script>

<template>
  <div class="collect-page">
    <section class="collect-panel">
      <div class="page-title">
        <el-icon><Collection/></el-icon>
        <div>
          <h2>我的收藏</h2>
          <span>集中查看已收藏的校园帖子</span>
        </div>
      </div>
      <div v-loading="loading">
        <el-empty v-if="!loading && topics.length === 0" description="暂无收藏"/>
        <article v-for="topic in topics" :key="topic.id" class="topic-card" @click="router.push(`/index/topic-detail/${topic.id}`)">
          <div class="topic-meta">
            <span>{{ topic.username || '匿名用户' }}</span>
            <span>{{ formatTime(topic.time) }}</span>
          </div>
          <h3>{{ topic.title }}</h3>
          <p>{{ topic.text || '暂无摘要' }}</p>
          <div v-if="topic.images && topic.images.length" class="topic-images">
            <el-image v-for="image in topic.images.slice(0, 3)" :key="image" :src="objectUrl(image)" fit="cover"/>
          </div>
          <div class="topic-stats">
            <span><el-icon><Pointer/></el-icon>{{ topic.like }}</span>
            <span><el-icon><Star/></el-icon>{{ topic.collect }}</span>
          </div>
        </article>
      </div>
    </section>
  </div>
</template>

<style scoped>
.collect-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 18px;
}

.collect-panel,
.topic-card {
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
}

.collect-panel {
  padding: 18px;
}

.page-title,
.topic-meta,
.topic-stats {
  display: flex;
  align-items: center;
  gap: 10px;
}

.page-title {
  margin-bottom: 16px;
}

.page-title h2 {
  margin: 0 0 4px;
}

.page-title span,
.topic-meta {
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.topic-card {
  padding: 16px;
  margin-bottom: 12px;
  cursor: pointer;
}

.topic-card:hover {
  border-color: var(--el-color-primary);
}

.topic-card h3 {
  margin: 10px 0 8px;
}

.topic-card p {
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

.topic-stats {
  margin-top: 12px;
  color: var(--el-text-color-secondary);
}
</style>
