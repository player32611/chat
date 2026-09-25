module.exports = {
	root: true,
	env: {
		browser: true,
		node: true,
		es2021: true,
	},
	globals: {
		uni: 'readonly',
		wx: 'readonly',
		plus: 'readonly',
		getApp: 'readonly',
		getCurrentPages: 'readonly',
	},
	parser: 'vue-eslint-parser',
	parserOptions: {
		parser: '@typescript-eslint/parser',
		ecmaVersion: 'latest',
		sourceType: 'module',
	},
	extends: [
		'eslint:recommended',
		'plugin:vue/vue3-recommended',
		'plugin:@typescript-eslint/recommended',
		'plugin:prettier/recommended',
	],
	plugins: ['@typescript-eslint'],
	rules: {
		'no-console': 'off',
		'no-debugger': process.env.NODE_ENV === 'production' ? 'error' : 'off',
		'@typescript-eslint/no-explicit-any': 'error',
		'vue/multi-word-component-names': 'off',
		'vue/html-indent': ['error', 'tab'],
		'vue/max-attributes-per-line': 'off',
		'vue/singleline-html-element-content-newline': 'off',
	},
	ignorePatterns: ['dist', 'node_modules', 'unpackage'],
}
