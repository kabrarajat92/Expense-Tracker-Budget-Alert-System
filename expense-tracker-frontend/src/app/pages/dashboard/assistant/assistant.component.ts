import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AiService } from '../../../services/ai.service';

interface ChatMessage {
  role: 'user' | 'assistant';
  text: string;
}

@Component({
  selector: 'app-assistant',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './assistant.component.html',
  styleUrl: './assistant.component.scss'
})
export class AssistantComponent {
  messages: ChatMessage[] = [
    { role: 'assistant', text: 'Ask me anything about your spending — I can see your expenses and budgets.' }
  ];
  currentMessage = '';
  loading = false;

  constructor(private aiService: AiService) {}

  send(): void {
    const msg = this.currentMessage.trim();
    if (!msg || this.loading) return;

    this.messages.push({ role: 'user', text: msg });
    this.currentMessage = '';
    this.loading = true;

    this.aiService.chat(msg).subscribe({
      next: (res) => {
        this.messages.push({ role: 'assistant', text: res.reply });
        this.loading = false;
      },
      error: () => {
        this.messages.push({ role: 'assistant', text: 'Sorry, I could not reach the AI service. Please try again.' });
        this.loading = false;
      }
    });
  }

  quickAnalyze(): void {
    this.loading = true;
    this.messages.push({ role: 'user', text: 'Give me a full spending analysis.' });
    this.aiService.analyze().subscribe({
      next: (res) => {
        this.messages.push({ role: 'assistant', text: res.analysis });
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  quickForecast(): void {
    this.loading = true;
    this.messages.push({ role: 'user', text: 'Forecast whether I will exceed my budgets this month.' });
    this.aiService.forecast().subscribe({
      next: (res) => {
        this.messages.push({ role: 'assistant', text: res.forecast });
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }
}