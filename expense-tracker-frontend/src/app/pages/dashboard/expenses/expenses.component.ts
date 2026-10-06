import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ExpenseService, Expense } from '../../../services/expense.service';

@Component({
  selector: 'app-expenses',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './expenses.component.html',
  styleUrl: './expenses.component.scss'
})
export class ExpensesComponent implements OnInit {
  expenses: Expense[] = [];
  newExpense: Expense = this.emptyExpense();
  saving = false;

  constructor(private expenseService: ExpenseService) {}

  ngOnInit(): void {
    this.load();
  }

  emptyExpense(): Expense {
    return { category: '', amount: 0, description: '', expenseDate: new Date().toISOString().split('T')[0] };
  }

  load(): void {
    this.expenseService.getExpenses().subscribe((data) => {
      this.expenses = [...data].sort((a, b) => (b.expenseDate > a.expenseDate ? 1 : -1));
    });
  }

  addExpense(): void {
    if (!this.newExpense.category || !this.newExpense.amount) return;
    this.saving = true;
    this.expenseService.addExpense(this.newExpense).subscribe({
      next: () => {
        this.newExpense = this.emptyExpense();
        this.saving = false;
        this.load();
      },
      error: () => (this.saving = false)
    });
  }
}