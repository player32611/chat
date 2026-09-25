import { BASE_URL, CODE_SUCCESS, MAX_IMAGE_SIZE } from '@/utils/constants'
import { getToken } from '@/utils/token'

/** 读取本地文件大小（字节） */
function getFileSize(filePath: string): Promise<number> {
	return new Promise<number>((resolve, reject) => {
		uni.getFileInfo({
			filePath,
			success: (res) => resolve(res.size),
			fail: (err) => reject(err),
		})
	})
}

/** 上传图片，返回可访问 URL（超过大小限制时本地拦截并提示） */
export async function uploadImage(filePath: string): Promise<string> {
	const size = await getFileSize(filePath).catch(() => -1)
	if (size > MAX_IMAGE_SIZE) {
		uni.showToast({ title: '文件过大，单个文件不能超过 10MB', icon: 'none' })
		throw new Error('文件过大')
	}
	return doUpload(filePath)
}

function doUpload(filePath: string): Promise<string> {
	return new Promise<string>((resolve, reject) => {
		uni.uploadFile({
			url: `${BASE_URL}/api/file/upload`,
			filePath,
			name: 'file',
			header: { Authorization: `Bearer ${getToken()}` },
			success: (res) => {
				try {
					const result = JSON.parse(res.data) as { code: number; msg: string; data: string }
					if (result.code === CODE_SUCCESS) {
						resolve(result.data)
					} else {
						uni.showToast({ title: result.msg || '上传失败', icon: 'none' })
						reject(result)
					}
				} catch {
					reject(new Error('上传响应解析失败'))
				}
			},
			fail: (err) => {
				uni.showToast({ title: '上传失败', icon: 'none' })
				reject(err)
			},
		})
	})
}
