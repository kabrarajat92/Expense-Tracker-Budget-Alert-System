import { Injectable, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly storageKey = 'expense-tracker-theme';
  isLight = signal<boolean>(this.getInitialTheme());

  constructor() {
    this.applyTheme(this.isLight());
  }

  private getInitialTheme(): boolean {
    const saved = localStorage.getItem(this.storageKey);
    return saved === 'light';
  }

  toggle(): void {
    const next = !this.isLight();
    this.isLight.set(next);
    this.applyTheme(next);
    localStorage.setItem(this.storageKey, next ? 'light' : 'dark');
  }

  private applyTheme(light: boolean): void {
    document.documentElement.classList.toggle('light-theme', light);
  }
}