import { clsx, type ClassValue } from 'clsx'
import { twMerge } from 'tailwind-merge'

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

export function formatarCPF(cpf: string): string {
  const cpfLimpo = cpf.replace(/\D/g, '')
  if (cpfLimpo.length !== 11) return cpf
  return cpfLimpo.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4')
}

export function formatarTelefone(telefone: string): string {
  const telLimpo = telefone.replace(/\D/g, '')
  if (telLimpo.length !== 11) return telefone
  return telLimpo.replace(/(\d{2})(\d{5})(\d{4})/, '($1) $2-$3')
}

export function abrirLinkExterno(url: string): void {
  window.open(url, '_blank', 'noopener,noreferrer')
}
