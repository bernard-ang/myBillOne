import { ChangeDetectionStrategy, Component, ElementRef, Input, OnDestroy, ViewChild } from '@angular/core';

@Component({
  selector: 'grabbill-client-html-preview',
  templateUrl: './html-preview.component.html',
  styleUrls: ['./html-preview.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HtmlPreviewComponent implements OnDestroy {
  _isOnLoad = false;
  _html!: string;

  @Input('html')
  set html(value: string) {
    this._html = value;

    if (this._isOnLoad) {
      this.setIframeReady(this.iframe, this._html!);
    }
  }

  @ViewChild('iframe') iframe!: ElementRef;

  onLoad() {
    this._isOnLoad = true;
    this.setIframeReady(this.iframe, this._html!);
  }

  private setIframeReady(iframe: ElementRef, html: string): void {
    if (iframe) {
      const iframeElement = iframe.nativeElement;
      const win: Window = iframeElement.contentWindow;

      const doc: Document = win.document;
      doc.open();
      doc.write(html);
      doc.close();

      // delay to wait image loaded
      setTimeout(() => {
        if (iframeElement.contentWindow) {
          iframeElement.width = iframeElement.contentWindow.document.body.scrollWidth;
          iframeElement.height = iframeElement.contentWindow.document.body.scrollHeight;
        }
      }, 500);
    } else {
      setTimeout(() => {
        this.setIframeReady(this.iframe, this._html!);
      }, 500);
    }
  }

  ngOnDestroy(): void {}
}
