<script setup lang="ts">
import { ref } from 'vue'
import AuthView from './views/AuthView.vue'
import DashboardView from './views/DashboardView.vue'
import { clearSession, loadSession, saveSession } from './session'
import type { AuthSession } from './types/auth'

const session = ref<AuthSession | null>(loadSession())

function handleLogin(nextSession: AuthSession) {
  session.value = nextSession
  saveSession(nextSession)
}

function handleLogout() {
  session.value = null
  clearSession()
}
</script>

<template>
  <DashboardView v-if="session" :session="session" @logout="handleLogout" />
  <AuthView v-else @login="handleLogin" />
</template>
