import { Component } from '@angular/core';
import { RouterOutlet, RouterLink } from '@angular/router';
import { NotificationToastComponent } from './components/notification-toast/notification-toast.component';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, NotificationToastComponent],
  templateUrl: './app.html',
  styleUrls: ['./app.css']
})
export class App {


}
