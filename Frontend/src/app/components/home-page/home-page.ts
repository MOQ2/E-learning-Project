import { Component } from '@angular/core';
import { NavBar } from '../nav-bar/nav-bar';
import { Footer } from '../footer/footer';
import { SidebarComponent } from '../courses/sidebare/sidebar';
@Component({
  selector: 'app-home-page',
  standalone: true,
  imports: [NavBar, Footer ],
  templateUrl: './home-page.html',
  styleUrls: ['./home-page.css']
})
export class HomePage {

}
