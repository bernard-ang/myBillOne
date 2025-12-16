/**
 * https://stackoverflow.com/questions/2970525/converting-any-string-into-camel-case
 */
export const openBlob = (blob: Blob, FILENAME: string) => {
  let url = window.URL.createObjectURL(blob)
  let a = document.createElement('a')
  document.body.appendChild(a)
  a.href = url
  a.download = FILENAME
  a.click()
  window.URL.revokeObjectURL(url)
  document.body.removeChild(a)
}
