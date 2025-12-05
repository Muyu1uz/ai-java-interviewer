// Utility to handle streaming responses
export async function fetchStream(url: string, options: RequestInit, onChunk: (chunk: string) => void) {
  const token = localStorage.getItem('token')
  const headers = {
    ...options.headers,
    'Authorization': token ? `Bearer ${token}` : '',
    'Content-Type': 'application/json'
  }

  const response = await fetch(`/api${url}`, {
    ...options,
    headers
  })

  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`)
  }

  const reader = response.body?.getReader()
  const decoder = new TextDecoder('utf-8')

  if (!reader) {
    throw new Error('Response body is null')
  }

  let buffer = ''

  while (true) {
    const { done, value } = await reader.read()
    if (done) break
    
    buffer += decoder.decode(value, { stream: true })
    
    const lines = buffer.split('\n')
    buffer = lines.pop() || ''
    
    for (const line of lines) {
      const trimmed = line.trim()
      if (!trimmed) continue
      if (trimmed.startsWith('data:')) {
        onChunk(trimmed.slice(5))
      }
    }
  }

  if (buffer.trim().startsWith('data:')) {
    onChunk(buffer.trim().slice(5))
  }
}
