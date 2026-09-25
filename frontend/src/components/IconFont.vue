<template>
	<text class="iconfont" :style="style">{{ char }}</text>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { ICON_MAP } from '@/utils/icon'

const props = withDefaults(
	defineProps<{
		name: string
		size?: number
		color?: string
	}>(),
	{
		size: 32,
		color: '',
	}
)

const char = computed(() => {
	const code = ICON_MAP[props.name]
	return code === undefined ? '' : String.fromCodePoint(code)
})

const style = computed(() => ({
	fontSize: `${props.size}rpx`,
	color: props.color || undefined,
}))
</script>
