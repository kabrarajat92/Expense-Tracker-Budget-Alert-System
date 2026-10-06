import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AiService {
  private readonly baseUrl = `${environment.apiUrl}/api/auth`;

  constructor(private http: HttpClient) {}

  analyze(): Observable<{ analysis: string }> {
    return this.http.get<{ analysis: string }>(`${this.baseUrl}/analyze`);
  }

  forecast(): Observable<{ forecast: string }> {
    return this.http.get<{ forecast: string }>(`${this.baseUrl}/forecast`);
  }

  suggestBudget(monthlyIncome: number): Observable<{ suggestions: string }> {
    return this.http.post<{ suggestions: string }>(`${this.baseUrl}/suggest-budget`, { monthlyIncome });
  }

  chat(message: string): Observable<{ reply: string }> {
    return this.http.post<{ reply: string }>(`${this.baseUrl}/chat`, { message });
  }
}