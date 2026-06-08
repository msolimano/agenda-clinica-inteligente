import { Cpu } from 'lucide-react';
import { AIProviderStatusBadge } from './AIProviderStatusBadge';
import type { AIProviderStatus } from './documents.types';

interface AIProviderNoticeProps {
  status: AIProviderStatus | null;
  error: string | null;
}

export function AIProviderNotice({ status, error }: AIProviderNoticeProps) {
  const provider = status?.activeProvider ?? 'mock';
  const model = status?.modelName || 'No configurado';
  const promptVersion = status?.promptVersion || 'DOCUMENT-AI-V1';
  const isOpenAINotConfigured = provider === 'openai' && status?.configured === false;

  return (
    <section className="ai-provider-panel" aria-label="Proveedor IA documental">
      <div className="ai-provider-panel__header">
        <span className="ai-provider-panel__icon"><Cpu aria-hidden="true" size={20} /></span>
        <div className="ai-provider-panel__heading">
          <h2 className="ai-provider-panel__title">Proveedor IA</h2>
          <p className="ai-provider-panel__subtitle">Prompt {promptVersion}</p>
        </div>
        <AIProviderStatusBadge status={status} />
      </div>
      <div className="ai-provider-panel__body">
        {error ? <div className="ai-provider-panel__error" role="alert">{error}</div> : null}
        <p className="ai-provider-panel__meta">Modelo: {model}</p>
        {isOpenAINotConfigured ? <div className="ai-provider-panel__warning">OpenAI está seleccionado, pero falta API key o modelo. El análisis fallará de forma controlada.</div> : null}
      </div>
    </section>
  );
}
