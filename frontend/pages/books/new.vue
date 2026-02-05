<script setup lang="ts">
import type { CreateBookRequest, Book } from "~/types/book";

const api = useApi();

const title = ref("");
const author = ref("");

const pending = ref(false);
const error = ref<string | null>(null);

const canSave = computed(() => title.value.trim().length > 0 && !pending.value);

async function onSave() {
  error.value = null;
  pending.value = true;
  try {
    const payload: CreateBookRequest = {
      title: title.value.trim(),
      author: author.value.trim() ? author.value.trim() : null,
    };

    // 作成後に一覧へ戻す（v1）
    await api.post<CreateBookRequest, Book>("/admin/books", payload);
    await navigateTo("/books");
  } catch (e) {
    error.value = (e as Error).message;
  } finally {
    pending.value = false;
  }
}
</script>

<template>
  <main class="p-6 max-w-3xl mx-auto">
    <header class="mb-6">
      <NuxtLink to="/books" class="underline opacity-80">← Back</NuxtLink>
      <h1 class="text-2xl font-bold mt-3">New Book</h1>
      <p class="opacity-70 mt-1">Create a book to attach insights.</p>
    </header>

    <section v-if="error" class="border rounded p-4 mb-4">
      <p class="font-semibold">Error</p>
      <p class="opacity-80 mt-1">{{ error }}</p>
    </section>

    <form class="space-y-4" @submit.prevent="onSave">
      <div>
        <label class="block font-medium mb-1"> Title <span class="text-red-600">*</span> </label>
        <input
          v-model="title"
          class="w-full border rounded p-2"
          placeholder="e.g. Effective Kotlin"
          :disabled="pending"
        />
      </div>

      <div>
        <label class="block font-medium mb-1">Author</label>
        <input
          v-model="author"
          class="w-full border rounded p-2"
          placeholder="e.g. Marcin Moskala"
          :disabled="pending"
        />
      </div>

      <div class="pt-2 flex gap-3">
        <button
          type="submit"
          class="px-4 py-2 border rounded"
          :disabled="!canSave"
          :class="!canSave ? 'opacity-50 cursor-not-allowed' : ''"
        >
          {{ pending ? "Saving..." : "Save" }}
        </button>

        <NuxtLink to="/books" class="px-4 py-2 border rounded inline-block"> Cancel </NuxtLink>
      </div>
    </form>
  </main>
</template>
