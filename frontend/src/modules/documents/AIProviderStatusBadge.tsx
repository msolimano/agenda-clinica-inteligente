import type { AIProviderStatus } from './documents.types';

interface AIProviderStatusBadgeProps {
  status: AIProviderStatus | null;
}

export function AIProviderStatusBadge({ status }: AIProviderStatusBadgeProps) {
  const provider = status?.activeProvider ?? 'mock';
  const configured = status?.configured ?? true;
  const label = provider === 'openai' ? 'OpenAI' : 'Mock';
  return <span className={`ai-provider-status ai-provider-status--${configured ? 'ready' : 'warning'}`}>{label}</span>;
}
