import type { ESLint } from 'eslint';

const config: ESLint.ConfigData = {
  root: true,
  env: {
    browser: true,
    es2021: true,
    node: true,
  },
  extends: [
    'eslint:recommended',
    'plugin:vue/vue3-recommended',
    'plugin:@typescript-eslint/recommended',
  ],
  parser: '@typescript-eslint/parser',
  parserOptions: {
    ecmaVersion: 12,
    sourceType: 'module',
  },
  plugins: ['vue', '@typescript-eslint'],
  rules: {
    'vue/no-unused-vars': 'warn',
    '@typescript-eslint/no-explicit-any': 'off',
    'no-console': process.env.NODE_ENV === 'production' ? 'warn' : 'off',
  },
};

export default config;