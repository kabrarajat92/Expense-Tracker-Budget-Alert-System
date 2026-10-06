import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificationService, Notification } from '../../../services/notification.service';

@Component({
  selector: 'app-alerts',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './alerts.component.html',
  styleUrl: './alerts.component.scss'
})
export class AlertsComponent implements OnInit {
  notifications: Notification[] = [];

  constructor(private notificationService: NotificationService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.notificationService.getAll().subscribe((data) => {
      this.notifications = [...data].sort((a, b) => (b.createdAt > a.createdAt ? 1 : -1));
    });
  }

  markRead(id: string): void {
    this.notificationService.markRead(id).subscribe(() => this.load());
  }
}