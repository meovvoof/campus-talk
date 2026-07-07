<script setup>
import { get } from '@/net'
import router from '@/router'
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { Search } from '@element-plus/icons-vue'

const route = useRoute()
const loading = ref(false)
const results = ref([])
const keyword = computed(() => route.query.keyword || '')

function plainHighlight(item) {
  const fields = item.highlight || {}
  const values = Object.values(fields).flat()
  return values.length ? values.join(' ... ') : item.intro
}

function loadResults() {
  if (!keyword.value) {
    results.value = []
    return
  }
  loading.value = true
  get(`/api/forum/search-topic?keyword=${encodeURIComponent(keyword.value)}`, data => {
    results.value = data || []
    loading.value = false
  }, () => {
    results.value = []
    loading.value = false
  })
}

watch(keyword, loadResults)
onMounted(loadResults)
</script>

<template>
  <div class="search-page">
    <section class="search-panel">
      <div class="page-title">
        <el-icon><Search/></el-icon>
        <div>
          <h2>搜索结果</h2>
          <span>{{ keyword ? `关键词：${keyword}` : '请输入关键词搜索帖子' }}</span>
        </div>
      </div>

      <div v-loading="loading">
        <el-empty v-if="!loading && results.length === 0" description="暂无匹配帖子"/>
        <article v-for="item in results" :key="item.id" class="result-card" @click="router.push(`/index/topic-detail/${item.id}`)">
          <h3>{{ item.title }}</h3>
          <p>{{ plainHighlight(item) }}</p>
        </article>
      </div>
    </section>
  </div>
</template>

<style scoped>
.search-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 18px;
}

.search-panel,
.result-card {
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
}

.search-panel {
  padding: 18px;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}

.page-title h2 {
  margin: 0 0 4px;
}

.page-title span {
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.result-card {
  padding: 16px;
  margin-bottom: 12px;
  cursor: pointer;
}

.result-card:hover {
  border-color: var(--el-color-primary);
}

.result-card h3 {
  margin: 0 0 8px;
}

.result-card p {
  margin: 0;
  color: var(--el-text-color-regular);
  line-height: 1.7;
}
</style>
