import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { NavBar } from '../nav-bar/nav-bar';
import { Footer } from '../footer/footer';
@Component({
  selector: 'app-home-page',
  standalone: true,
  imports: [NavBar, Footer, RouterLink],
  templateUrl: './home-page.html',
  styleUrls: ['./home-page.css']
})
export class HomePage {

}
