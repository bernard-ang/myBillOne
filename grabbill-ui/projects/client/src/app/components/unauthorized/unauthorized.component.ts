import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';

@Component({
  selector: 'grabbill-client-unauthorized',
  templateUrl: './unauthorized.component.html',
  styleUrls: ['./unauthorized.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class UnauthorizedComponent implements OnInit {

  constructor() { }

  ngOnInit(): void {
  }

}
