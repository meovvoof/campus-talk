<script setup>
import { get, post } from '@/net'
import { deltaBlocks, deltaFromText, objectUrl } from '@/utils/delta'
import router from '@/router'
import { computed, onMounted, reactive, ref } from 'vue'
import { ChatLineRound, EditPen, Pointer, Star } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  tid: String
})

const loading = ref(true)
const commentLoading = ref(false)
const topic = ref(null)
const comments = ref([])
const commentForm = reactive({
  content: '',
  quote: -1
})

const blocks = computed(() => deltaBlocks(topic.value?.content))

function formatTime(time) {
  return time ? new Date(time).toLocaleString() : ''
}

function loadTopic() {
  loading.value = true
  get(`/api/forum/topic?tid=${props.tid}`, data => {
    topic.value = data
    loading.value = false
  }, message => {
    loading.value = false
    ElMessage.warning(message)
    router.push('/index/forum')
  })
}

function loadComments() {
  commentLoading.value = true
  get(`/api/forum/comments?tid=${props.tid}&page=0`, data => {
    comments.value = data || []
    commentLoading.value = false
  }, () => {
    comments.value = []
    commentLoading.value = false
  })
}

function setInteract(type) {
  if (!topic.value?.interact) return
  const next = !topic.value.interact[type]
  get(`/api/forum/interact?tid=${props.tid}&type=${type}&state=${next}`, () => {
    topic.value.interact[type] = next
  })
}

function submitComment() {
  if (!commentForm.content.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  post('/api/forum/add-comment', {
    tid: Number(props.tid),
    content: JSON.stringify(deltaFromText(commentForm.content)),
    quote: commentForm.quote
  }, () => {
    ElMessage.success('回复已发布')
    commentForm.content = ''
    commentForm.quote = -1
    loadComments()
    loadTopic()
  })
}

function quoteComment(comment) {
  commentForm.quote = comment.id
}

function commentText(comment) {
  return deltaBlocks(comment.content)
      .filter(item => item.type === 'text')
      .map(item => item.value)
      .join('')
}

onMounted(() => {
  loadTopic()
  loadComments()
})
</script>

<template>
  <div class="detail-page" v-loading="loading">
    <template v-if="topic">
      <section class="topic-detail">
        <div class="detail-head">
          <div>
            <h2>{{ topic.title }}</h2>
            <div class="meta">
              <span>{{ topic.user?.username || '匿名用户' }}</span>
              <span>{{ formatTime(topic.time) }}</span>
              <el-tag v-if="topic.locked" size="small" type="warning">已锁定</el-tag>
            </div>
          </div>
          <el-button :icon="EditPen" @click="router.push(`/index/topic-edit/${topic.id}`)">编辑</el-button>
        </div>

        <div class="content-blocks">
          <template v-for="(block, index) in blocks" :key="index">
            <p v-if="block.type === 'text'">{{ block.value }}</p>
            <el-image v-else-if="block.type === 'image'" :src="objectUrl(block.value)" fit="contain"/>
          </template>
        </div>

        <div class="interact-bar">
          <el-button :type="topic.interact?.like ? 'primary' : 'default'" :icon="Pointer" @click="setInteract('like')">
            {{ topic.interact?.like ? '已点赞' : '点赞' }}
          </el-button>
          <el-button :type="topic.interact?.collect ? 'primary' : 'default'" :icon="Star" @click="setInteract('collect')">
            {{ topic.interact?.collect ? '已收藏' : '收藏' }}
          </el-button>
        </div>
      </section>

      <section class="comment-panel">
        <div class="section-title">
          <el-icon><ChatLineRound/></el-icon>
          回复
          <span>{{ topic.comments || 0 }}</span>
        </div>
        <div class="comment-form">
          <el-alert v-if="topic.locked" type="warning" :closable="false" title="主题已锁定，无法继续回复"/>
          <template v-else>
            <el-alert
                v-if="commentForm.quote > 0"
                type="info"
                :closable="true"
                @close="commentForm.quote = -1"
                title="正在引用一条回复"/>
            <el-input
                v-model="commentForm.content"
                type="textarea"
                :rows="4"
                maxlength="2000"
                show-word-limit
                placeholder="写下你的回复"/>
            <el-button type="primary" style="margin-top: 10px" @click="submitComment">发布回复</el-button>
          </template>
        </div>

        <div v-loading="commentLoading">
          <el-empty v-if="!commentLoading && comments.length === 0" description="暂无回复"/>
          <div v-for="comment in comments" :key="comment.id" class="comment-item">
            <div class="comment-meta">
              <span>{{ comment.user?.username || '匿名用户' }}</span>
              <span>{{ formatTime(comment.time) }}</span>
            </div>
            <blockquote v-if="comment.quote">{{ comment.quote }}</blockquote>
            <p>{{ commentText(comment) }}</p>
            <el-button text size="small" @click="quoteComment(comment)">引用</el-button>
          </div>
        </div>
      </section>
    </template>
  </div>
</template>

<style scoped>
.detail-page {
  max-width: 920px;
  margin: 0 auto;
  padding: 18px;
}

.topic-detail,
.comment-panel {
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
  padding: 18px;
}

.comment-panel {
  margin-top: 14px;
}

.detail-head {
  display: flex;
  justify-content: space-between;
  gap: 16px;
}

.detail-head h2 {
  margin: 0 0 10px;
  font-size: 24px;
}

.meta,
.comment-meta,
.section-title,
.interact-bar {
  display: flex;
  align-items: center;
  gap: 10px;
}

.meta,
.comment-meta {
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.content-blocks {
  padding: 20px 0;
  line-height: 1.8;
  white-space: pre-wrap;
}

.content-blocks .el-image {
  display: block;
  max-height: 460px;
  margin: 12px 0;
  border-radius: 8px;
  overflow: hidden;
}

.interact-bar {
  border-top: 1px solid var(--el-border-color-lighter);
  padding-top: 14px;
}

.section-title {
  font-weight: 700;
  margin-bottom: 12px;
}

.comment-form {
  margin-bottom: 16px;
}

.comment-form .el-alert {
  margin-bottom: 10px;
}

.comment-item {
  border-top: 1px solid var(--el-border-color-lighter);
  padding: 14px 0;
}

.comment-item p {
  margin: 8px 0;
  white-space: pre-wrap;
}

blockquote {
  margin: 8px 0;
  padding: 8px 10px;
  background: var(--el-fill-color-lighter);
  border-left: 3px solid var(--el-color-primary);
  color: var(--el-text-color-secondary);
}
</style>
