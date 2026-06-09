import { Search, X } from 'lucide-react';
import { useState } from 'react';
import { searchDiagnosisCatalog } from './diagnosisCatalogApi';
import type { DiagnosisCatalog } from './diagnosisCatalog.types';
import './diagnosis-catalog.css';

interface DiagnosisSearchInputProps {
  organizationId?: string | null;
  disabled?: boolean;
  selectedLabel?: string | null;
  onSelect: (diagnosis: DiagnosisCatalog) => void;
  onClear: () => void;
}

export function DiagnosisSearchInput({ organizationId, disabled, selectedLabel, onSelect, onClear }: DiagnosisSearchInputProps) {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState<DiagnosisCatalog[]>([]);
  const [isSearching, setIsSearching] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function handleSearch() {
    setIsSearching(true);
    setError(null);
    try {
      const response = await searchDiagnosisCatalog(query, organizationId);
      setResults(response);
    } catch (currentError) {
      setError(currentError instanceof Error ? currentError.message : 'No fue posible buscar diagnosticos');
    } finally {
      setIsSearching(false);
    }
  }

  function handleSelect(diagnosis: DiagnosisCatalog) {
    onSelect(diagnosis);
    setQuery(diagnosis.diagnosisDisplay);
    setResults([]);
  }

  return (
    <div className="diagnosis-search-input">
      <label className="diagnosis-search-input__label">
        Buscar en catalogo
        <span className="diagnosis-search-input__control">
          <Search aria-hidden="true" size={17} />
          <input value={query} onChange={(event) => setQuery(event.target.value)} onKeyDown={(event) => { if (event.key === 'Enter') { event.preventDefault(); void handleSearch(); } }} disabled={disabled} placeholder="Diagnostico, codigo o categoria" />
          <button type="button" disabled={disabled || isSearching} onClick={() => void handleSearch()}>Buscar</button>
        </span>
      </label>

      {selectedLabel ? (
        <div className="diagnosis-search-input__selected">
          <span>{selectedLabel}</span>
          <button type="button" disabled={disabled} onClick={onClear} aria-label="Quitar diagnostico de catalogo"><X aria-hidden="true" size={15} /></button>
        </div>
      ) : null}

      {error ? <div className="diagnosis-search-input__error" role="alert">{error}</div> : null}
      {results.length ? (
        <div className="diagnosis-search-input__results">
          {results.map((diagnosis) => (
            <button key={diagnosis.id} type="button" disabled={disabled} onClick={() => handleSelect(diagnosis)}>
              <strong>{diagnosis.diagnosisDisplay}</strong>
              <span>{diagnosis.category || 'Sin categoria'}</span>
              {diagnosis.diagnosisCode ? <small>{diagnosis.codeSystem ? `${diagnosis.codeSystem}: ` : ''}{diagnosis.diagnosisCode}</small> : null}
            </button>
          ))}
        </div>
      ) : null}
    </div>
  );
}
