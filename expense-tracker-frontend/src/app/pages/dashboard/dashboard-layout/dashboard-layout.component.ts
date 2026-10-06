import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { ThemeService } from '../../../services/theme.service';

@Component({
  selector: 'app-dashboard-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './dashboard-layout.component.html',
  styleUrl: './dashboard-layout.component.scss'
})
export class DashboardLayoutComponent {
  constructor(
    private authService: AuthService,
    private router: Router,
    public themeService: ThemeService
  ) {}

  get username(): string | null {
    return this.authService.getUsername();
  }

  get userInitial(): string {
    const name = this.username;
    return name ? name.charAt(0).toUpperCase() : '?';
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}