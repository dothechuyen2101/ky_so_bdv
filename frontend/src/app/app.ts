import { Component, HostBinding, ViewEncapsulation, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { PreferencesService } from './core/preferences.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.css',
  encapsulation: ViewEncapsulation.None,
})
export class App {
  private readonly preferences = inject(PreferencesService);

  @HostBinding('class.dark-mode')
  get darkMode(): boolean {
    return this.preferences.darkMode();
  }
}
