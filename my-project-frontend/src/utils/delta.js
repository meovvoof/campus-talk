function normalizeDelta(content) {
    if (!content) return { ops: [] }
    if (typeof content === 'object') return content
    try {
        return JSON.parse(content)
    } catch {
        return { ops: [{ insert: String(content) }] }
    }
}

function deltaFromText(text) {
    return {
        ops: [
            { insert: text || '' }
        ]
    }
}

function deltaText(content) {
    const delta = normalizeDelta(content)
    return (delta.ops || [])
        .map(op => typeof op.insert === 'string' ? op.insert : '')
        .join('')
}

function deltaBlocks(content) {
    const delta = normalizeDelta(content)
    return (delta.ops || []).map(op => {
        if (typeof op.insert === 'string') {
            return { type: 'text', value: op.insert }
        }
        if (op.insert && op.insert.image) {
            return { type: 'image', value: op.insert.image }
        }
        return { type: 'unknown', value: '' }
    }).filter(item => item.value)
}

function objectUrl(path) {
    if (!path) return ''
    if (/^https?:\/\//.test(path)) return path
    return `/images${path.startsWith('/') ? path : `/${path}`}`
}

export { deltaFromText, deltaText, deltaBlocks, objectUrl }
