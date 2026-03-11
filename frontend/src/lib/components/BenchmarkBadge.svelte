<script>
  export let score = 0;
  export let type = 'CPU';

  $: color = score >= 15000 ? 'bg-green-500' : score >= 8000 ? 'bg-yellow-500' : 'bg-red-500';
  $: label = score >= 15000 ? 'High-End' : score >= 8000 ? 'Mid-Range' : 'Budget';
</script>

{#if score}
  <span class="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-semibold text-white {color}">
    {type}: {score.toLocaleString()} <span class="opacity-80">({label})</span>
  </span>
{/if}
