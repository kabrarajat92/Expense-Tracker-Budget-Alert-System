import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  username: string;
  role: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly baseUrl = `${environment.apiUrl}/api/auth`;

  // In-memory token storage — cleared on page refresh, safer against XSS than localStorage
  private tokenSignal = signal<string | null>(
    sessionStorage.getItem('token')
  );
  private usernameSignal = signal<string | null>(null);

  constructor(private http: HttpClient) {}

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/login`, request).pipe(
      tap((response) => {
        this.tokenSignal.set(response.token);
        sessionStorage.setItem('token', response.token);
        this.usernameSignal.set(response.username);
      })
    );
  }

  register(request: RegisterRequest): Observable<any> {
    return this.http.post(`${this.baseUrl}/register`, request);
  }

  logout(): void {
    this.tokenSignal.set(null);
    this.usernameSignal.set(null);
    sessionStorage.removeItem('token');
  }

  getToken(): string | null {
    return this.tokenSignal();
  }

  getUsername(): string | null {
    return this.usernameSignal();
  }

  isLoggedIn(): boolean {
    return this.tokenSignal() !== null;
  }
}