<script setup>
import { get, post } from '@/net'
import { deltaFromText, deltaText } from '@/utils/delta'
import router from '@/router'
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { DocumentAdd, Select } from '@element-plus/icons-vue'

const props = defineProps({
  tid: String
})

const formRef = ref()
const loading = ref(false)
const types = ref([])
const form = reactive({
  id: null,
  title: '',
  type: null,
  content: ''
})

const editing = computed(() => !!props.tid)

const rules = {
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { min: 1, max: 30, message: '标题长度需要在 1-30 个字符之间', trigger: ['blur', 'change'] }
  ],
  type: [
    { required: true, message: '请选择分类', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请输入正文', trigger: 'blur' }
  ]
}

function loadTypes() {
  get('/api/forum/types', data => {
    types.value = data || []
    if (!form.type && types.value.length) {
      form.type = types.value[0].id
    }
  })
}

function loadTopic() {
  if (!editing.value) return
  loading.value = true
  get(`/api/forum/topic?tid=${props.tid}`, data => {
    form.id = data.id
    form.title = data.title
    form.type = data.type
    form.content = deltaText(data.content)
    loading.value = false
  }, message => {
    loading.value = false
    ElMessage.warning(message)
    router.push('/index/forum')
  })
}

function submit() {
  formRef.value.validate(valid => {
    if (!valid) return
    const payload = {
      title: form.title,
      type: form.type,
      content: deltaFromText(form.content)
    }
    if (editing.value) {
      payload.id = form.id
      post('/api/forum/update-topic', payload, () => {
        ElMessage.success('帖子已更新')
        router.push(`/index/topic-detail/${form.id}`)
      })
    } else {
      post('/api/forum/create-topic', payload, () => {
        ElMessage.success('帖子已发布')
        router.push('/index/forum')
      })
    }
  })
}

onMounted(() => {
  loadTypes()
  loadTopic()
})
</script>

<template>
  <div class="editor-page" v-loading="loading">
    <section class="editor-panel">
      <div class="editor-head">
        <el-icon><DocumentAdd/></el-icon>
        <div>
          <h2>{{ editing ? '编辑帖子' : '发布帖子' }}</h2>
          <span>正文将以 Delta JSON 格式提交到后端</span>
        </div>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" maxlength="30" show-word-limit placeholder="输入帖子标题"/>
        </el-form-item>
        <el-form-item label="分类" prop="type">
          <el-select v-model="form.type" style="width: 240px" placeholder="选择分类">
            <el-option v-for="item in types" :key="item.id" :value="item.id" :label="item.name"/>
          </el-select>
        </el-form-item>
        <el-form-item label="正文" prop="content">
          <el-input
              v-model="form.content"
              type="textarea"
              :rows="14"
              maxlength="20000"
              show-word-limit
              placeholder="写下正文内容"/>
        </el-form-item>
        <div class="actions">
          <el-button @click="router.back()">取消</el-button>
          <el-button type="primary" :icon="Select" @click="submit">{{ editing ? '保存修改' : '发布帖子' }}</el-button>
        </div>
      </el-form>
    </section>
  </div>
</template>

<style scoped>
.editor-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 18px;
}

.editor-panel {
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
  padding: 18px;
}

.editor-head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}

.editor-head h2 {
  margin: 0 0 4px;
  font-size: 22px;
}

.editor-head span {
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
