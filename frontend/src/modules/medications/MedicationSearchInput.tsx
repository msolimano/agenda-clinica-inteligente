import { Search, X } from 'lucide-react';
import { useState } from 'react';
import { searchMedications } from './medicationsApi';
import type { MedicationCatalog } from './medications.types';
import './medications.css';

interface MedicationSearchInputProps {
  organizationId?: string | null;
  disabled?: boolean;
  selectedLabel?: string | null;
  onSelect: (medication: MedicationCatalog) => void;
  onClear: () => void;
}

export function MedicationSearchInput({ organizationId, disabled, selectedLabel, onSelect, onClear }: MedicationSearchInputProps) {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState<MedicationCatalog[]>([]);
  const [isSearching, setIsSearching] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function handleSearch() {
    setIsSearching(true);
    setError(null);
    try {
      const response = await searchMedications(query, organizationId);
      setResults(response);
    } catch (currentError) {
      setError(currentError instanceof Error ? currentError.message : 'No fue posible buscar medicamentos');
    } finally {
      setIsSearching(false);
    }
  }

  function handleSelect(medication: MedicationCatalog) {
    onSelect(medication);
    setQuery(medication.medicationName);
    setResults([]);
  }

  return (
    <div className="medication-search-input">
      <label className="medication-search-input__label">
        Buscar en catalogo
        <span className="medication-search-input__control">
          <Search aria-hidden="true" size={17} />
          <input value={query} onChange={(event) => setQuery(event.target.value)} onKeyDown={(event) => { if (event.key === 'Enter') { event.preventDefault(); void handleSearch(); } }} disabled={disabled} placeholder="Nombre, principio activo o codigo" />
          <button type="button" disabled={disabled || isSearching} onClick={() => void handleSearch()}>Buscar</button>
        </span>
      </label>

      {selectedLabel ? (
        <div className="medication-search-input__selected">
          <span>{selectedLabel}</span>
          <button type="button" disabled={disabled} onClick={onClear} aria-label="Quitar medicamento de catalogo"><X aria-hidden="true" size={15} /></button>
        </div>
      ) : null}

      {error ? <div className="medication-search-input__error" role="alert">{error}</div> : null}
      {results.length ? (
        <div className="medication-search-input__results">
          {results.map((medication) => (
            <button key={medication.id} type="button" disabled={disabled} onClick={() => handleSelect(medication)}>
              <strong>{medication.medicationName}</strong>
              <span>{[medication.activeIngredient, medication.strength, medication.pharmaceuticalForm, medication.route].filter(Boolean).join(' · ') || 'Sin detalle'}</span>
              {medication.medicationCode ? <small>{medication.medicationCodeSystem ? `${medication.medicationCodeSystem}: ` : ''}{medication.medicationCode}</small> : null}
            </button>
          ))}
        </div>
      ) : null}
    </div>
  );
}
