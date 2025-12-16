import { Component, OnInit } from '@angular/core';
import buildInfo from '../../../../buildinfo.json';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.less'],
})
export class AppComponent implements OnInit {
  title = 'website';

  ngOnInit(): void {
    console.log('website', buildInfo.git);
  }
}
