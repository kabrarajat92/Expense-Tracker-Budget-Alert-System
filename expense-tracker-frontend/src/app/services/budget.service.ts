import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface Budget {
  id?: number;
  category: string;
  limitAmount: number;
}

@Injectable({ providedIn: 'root' })
export class BudgetService {
  private readonly baseUrl = `${environment.apiUrl}/api/budgets`;

  constructor(private http: HttpClient) {}

  getBudgets(): Observable<Budget[]> {
    return this.http.get<Budget[]>(this.baseUrl);
  }

  setBudget(budget: Budget): Observable<Budget> {
    return this.http.post<Budget>(this.baseUrl, budget);
  }
}