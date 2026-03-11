<script>
  import { createEventDispatcher } from 'svelte';
  import { api } from '$lib/api.js';
  import { authStore } from '$lib/stores/auth.js';

  export let dealId;
  export let upvotes = 0;
  export let downvotes = 0;
  export let userVote = null;

  const dispatch = createEventDispatcher();
  let loading = false;
  let auth;
  authStore.subscribe(v => auth = v);

  async function vote(voteType) {
    if (!auth.token) {
      window.location.href = '/login';
      return;
    }
    if (loading) return;
    loading = true;

    const prev = userVote;
    // Optimistic update
    if (userVote === voteType) {
      if (voteType === 'upvote') upvotes--;
      else downvotes--;
      userVote = null;
    } else {
      if (userVote === 'upvote') upvotes--;
      if (userVote === 'downvote') downvotes--;
      if (voteType === 'upvote') upvotes++;
      else downvotes++;
      userVote = voteType;
    }
    dispatch('vote', { voteType: userVote, upvotes, downvotes });

    try {
      if (prev === voteType) {
        await api.deals.removeVote(dealId);
      } else {
        await api.deals.vote(dealId, voteType);
      }
    } catch (e) {
      // Revert on error
      userVote = prev;
      dispatch('vote', { voteType: prev, upvotes, downvotes });
    } finally {
      loading = false;
    }
  }
</script>

<div class="flex items-center gap-1">
  <button
    on:click={() => vote('upvote')}
    class="flex items-center gap-1 px-2 py-1 rounded-lg transition-all
      {userVote === 'upvote' ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-600 hover:bg-green-50 hover:text-green-600'}"
    disabled={loading}
  >
    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 15l7-7 7 7"/>
    </svg>
    <span class="text-sm font-semibold">{upvotes}</span>
  </button>
  <button
    on:click={() => vote('downvote')}
    class="flex items-center gap-1 px-2 py-1 rounded-lg transition-all
      {userVote === 'downvote' ? 'bg-red-100 text-red-700' : 'bg-gray-100 text-gray-600 hover:bg-red-50 hover:text-red-600'}"
    disabled={loading}
  >
    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"/>
    </svg>
    <span class="text-sm font-semibold">{downvotes}</span>
  </button>
</div>
