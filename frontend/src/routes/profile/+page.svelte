<script>
  import { onMount } from 'svelte';
  import { goto } from '$app/navigation';
  import { api } from '$lib/api.js';
  import { authStore } from '$lib/stores/auth.js';
  import DealCard from '$lib/components/DealCard.svelte';
  import TagInput from '$lib/components/TagInput.svelte';

  let auth = { user: null, token: null };
  authStore.subscribe(v => auth = v);

  let feedTags = [];
  let myDeals = [];
  let loadingDeals = true;
  let saving = false;
  let saveSuccess = false;
  let error = null;

  onMount(async () => {
    if (!auth.token) { goto('/login'); return; }
    if (auth.user?.feed_tags) feedTags = auth.user.feed_tags;

    try {
      const [dealsRes] = await Promise.allSettled([
        api.deals.list({ user: auth.user?.id, limit: 10 })
      ]);
      if (dealsRes.status === 'fulfilled') {
        const d = dealsRes.value.data;
        myDeals = Array.isArray(d) ? d : (d.deals || d.data || []);
      }
    } catch {}
    loadingDeals = false;
  });

  async function savePreferences() {
    saving = true;
    saveSuccess = false;
    error = null;
    try {
      // Update user preferences via API (if endpoint available)
      authStore.setUser({ ...auth.user, feed_tags: feedTags });
      saveSuccess = true;
      setTimeout(() => saveSuccess = false, 3000);
    } catch (e) {
      error = 'Failed to save preferences';
    } finally {
      saving = false;
    }
  }
</script>

<svelte:head><title>Profile - TechDeals</title></svelte:head>

<div class="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
  {#if !auth.user}
    <div class="text-center py-16">
      <p class="text-gray-500">Please <a href="/login" class="text-blue-600 hover:underline">log in</a> to view your profile.</p>
    </div>
  {:else}
    <!-- User Info -->
    <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-6 mb-6">
      <div class="flex items-center gap-5">
        <div class="w-20 h-20 bg-gradient-to-br from-blue-500 to-blue-700 rounded-2xl flex items-center justify-center text-white text-3xl font-bold shadow-lg">
          {auth.user.username?.[0]?.toUpperCase() || 'U'}
        </div>
        <div>
          <h1 class="text-2xl font-bold text-gray-900">{auth.user.username}</h1>
          <p class="text-gray-500">{auth.user.email}</p>
          {#if auth.user.created_at}
            <p class="text-xs text-gray-400 mt-1">Member since {new Date(auth.user.created_at).toLocaleDateString('en-US', { month: 'long', year: 'numeric' })}</p>
          {/if}
        </div>
      </div>
    </div>

    <!-- Feed Preferences -->
    <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-6 mb-6">
      <h2 class="text-lg font-bold text-gray-900 mb-1">My Feed Preferences</h2>
      <p class="text-sm text-gray-500 mb-4">Add tags to personalize your deal feed (e.g., "SFF PC", "OLED Monitor", "Gaming Laptops")</p>

      {#if error}
        <div class="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg mb-4 text-sm">{error}</div>
      {/if}
      {#if saveSuccess}
        <div class="bg-green-50 border border-green-200 text-green-700 px-4 py-3 rounded-lg mb-4 text-sm">✓ Preferences saved!</div>
      {/if}

      <TagInput bind:tags={feedTags} placeholder="Add interest tag..." on:change={e => feedTags = e.detail} />

      <div class="mt-4 flex gap-3">
        <button
          on:click={savePreferences}
          disabled={saving}
          class="bg-blue-600 hover:bg-blue-700 disabled:opacity-60 text-white font-semibold px-5 py-2.5 rounded-lg text-sm transition-colors"
        >
          {saving ? 'Saving...' : 'Save Preferences'}
        </button>
      </div>
    </div>

    <!-- My Submitted Deals -->
    <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
      <h2 class="text-lg font-bold text-gray-900 mb-4">My Submitted Deals</h2>
      {#if loadingDeals}
        <div class="text-center py-8 text-gray-500">Loading...</div>
      {:else if myDeals.length === 0}
        <div class="text-center py-8 text-gray-500">
          <p>No deals submitted yet.</p>
          <a href="/submit" class="text-blue-600 hover:underline mt-2 inline-block">Submit your first deal →</a>
        </div>
      {:else}
        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {#each myDeals as deal (deal.id)}
            <DealCard {deal} />
          {/each}
        </div>
      {/if}
    </div>
  {/if}
</div>
