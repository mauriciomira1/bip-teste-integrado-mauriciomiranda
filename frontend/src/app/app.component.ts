import { Component } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <nav>
      <span class="nav-brand">BIP Benefícios</span>
      <div class="nav-links">
        <a routerLink="/beneficios" routerLinkActive="active" [routerLinkActiveOptions]="{exact:false}">Benefícios</a>
        <a routerLink="/transferir" routerLinkActive="active">Transferir</a>
      </div>
    </nav>
    <main>
      <router-outlet />
    </main>
  `,
  styles: [`
    nav {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 1rem 2rem;
      background: #1a1a2e;
      color: white;
    }
    .nav-brand { font-size: 1.2rem; font-weight: bold; }
    .nav-links { display: flex; gap: 1.5rem; }
    .nav-links a { color: #aaa; text-decoration: none; font-size: 0.95rem; }
    .nav-links a.active { color: white; font-weight: 600; }
    main { padding: 2rem; max-width: 960px; margin: 0 auto; }
  `]
})
export class AppComponent {}
