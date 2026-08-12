import { Injectable, signal } from '@angular/core';

export type Language = 'en' | 'vi';

const VI: Record<string, string> = {
  'Digital document operations, under control.': 'Vận hành chứng từ số, an toàn và kiểm soát.',
  'Search, verify, reconcile and deliver signed banking documents from one secure workspace.':
    'Tra cứu, xác minh, đối soát và cung cấp chứng từ ngân hàng đã ký trên một hệ thống bảo mật.',
  'Document integrity and audit trails': 'Toàn vẹn chứng từ và nhật ký kiểm toán',
  'Role-based operational access': 'Phân quyền truy cập theo vai trò',
  'Outbox processing visibility': 'Theo dõi xử lý hàng đợi Outbox',
  'Secure access': 'Truy cập bảo mật',
  'Sign in to T24R': 'Đăng nhập T24R',
  'Use your assigned operations account.': 'Sử dụng tài khoản nghiệp vụ được cấp.',
  Username: 'Tên đăng nhập',
  Password: 'Mật khẩu',
  'Enter username': 'Nhập tên đăng nhập',
  'Enter password': 'Nhập mật khẩu',
  'Sign in': 'Đăng nhập',
  'Sign out': 'Đăng xuất',
  'Protected by role-based access control': 'Được bảo vệ bằng cơ chế phân quyền theo vai trò',
  Workspace: 'Không gian làm việc',
  Overview: 'Tổng quan',
  Documents: 'Chứng từ',
  Operations: 'Vận hành',
  Administration: 'Quản trị',
  'Audit log': 'Nhật ký kiểm toán',
  'Users & access': 'Người dùng & truy cập',
  'API connected': 'API đã kết nối',
  'Operations overview': 'Tổng quan vận hành',
  'Document search': 'Tra cứu chứng từ',
  'Signing operations': 'Vận hành ký số',
  'Audit activity': 'Hoạt động kiểm toán',
  'Users & permissions': 'Người dùng & phân quyền',
  Administrator: 'Quản trị viên',
  Maker: 'Người lập',
  Checker: 'Người kiểm soát',
  'Document user': 'Người dùng chứng từ',
  'System at a glance': 'Tổng quan hệ thống',
  'Current document pipeline and reconciliation health.':
    'Tình trạng luồng chứng từ và đối soát hiện tại.',
  'Refresh data': 'Làm mới dữ liệu',
  'Total documents': 'Tổng chứng từ',
  'Across all branches': 'Trên tất cả chi nhánh',
  'Successfully signed': 'Đã ký thành công',
  'Ready for delivery': 'Sẵn sàng cung cấp',
  'Failed documents': 'Chứng từ lỗi',
  'Require attention': 'Cần xử lý',
  'Reconciliation issues': 'Lỗi đối soát',
  'Integrity exceptions': 'Ngoại lệ toàn vẹn',
  'Documents by branch': 'Chứng từ theo chi nhánh',
  'Current distribution': 'Phân bổ hiện tại',
  'No branch data available.': 'Chưa có dữ liệu chi nhánh.',
  'Recent outbox activity': 'Hoạt động Outbox gần đây',
  'total events': 'tổng sự kiện',
  'View all': 'Xem tất cả',
  'Search signed documents': 'Tra cứu chứng từ đã ký',
  'Find records by transaction, customer, type, branch or status.':
    'Tìm theo giao dịch, khách hàng, loại chứng từ, chi nhánh hoặc trạng thái.',
  results: 'kết quả',
  'Document code': 'Mã chứng từ',
  'Transaction ID': 'Mã giao dịch',
  'Customer ID': 'Mã khách hàng',
  Branch: 'Chi nhánh',
  'Document type': 'Loại chứng từ',
  Status: 'Trạng thái',
  Reconciliation: 'Đối soát',
  'All statuses': 'Tất cả trạng thái',
  'All results': 'Tất cả kết quả',
  'Contains document code': 'Chứa mã chứng từ',
  'Contains transaction ID': 'Chứa mã giao dịch',
  'Contains customer ID': 'Chứa mã khách hàng',
  'Contains branch': 'Chứa mã chi nhánh',
  'Contains document type': 'Chứa loại chứng từ',
  'Searching…': 'Đang tìm…',
  'Search documents': 'Tra cứu chứng từ',
  Document: 'Chứng từ',
  Customer: 'Khách hàng',
  'Type / branch': 'Loại / chi nhánh',
  Actions: 'Thao tác',
  Verify: 'Xác minh',
  'No documents match the current filters.': 'Không có chứng từ phù hợp bộ lọc.',
  'Document details': 'Chi tiết chứng từ',
  'Signing metadata': 'Thông tin ký số',
  'Signed at': 'Thời gian ký',
  Signer: 'Người ký',
  Certificate: 'Chứng thư',
  'Signed file': 'Tệp đã ký',
  'Select a document to inspect its signing metadata.': 'Chọn chứng từ để xem thông tin ký số.',
  'Snapshot comparison': 'So sánh dữ liệu',
  'Not selected': 'Chưa chọn',
  'Data path': 'Đường dẫn dữ liệu',
  'Snapshot A': 'Bản A',
  'Snapshot B': 'Bản B',
  'Snapshots are identical.': 'Hai bản dữ liệu giống nhau.',
  'Choose A and B on two document rows to compare their snapshots.':
    'Chọn A và B trên hai chứng từ để so sánh dữ liệu.',
  'Outbox processing': 'Xử lý Outbox',
  'Run signing jobs and monitor event delivery.': 'Chạy tác vụ ký số và theo dõi chuyển sự kiện.',
  'Maker workflow': 'Quy trình người lập',
  'Submit documents for approval': 'Trình chứng từ phê duyệt',
  'Generate source data and submit documents to the checker queue.':
    'Tạo dữ liệu nguồn và trình chứng từ vào hàng đợi kiểm soát.',
  'Submit for approval': 'Trình phê duyệt',
  'Checker workflow': 'Quy trình người kiểm soát',
  'Approve and sign documents': 'Phê duyệt và ký chứng từ',
  'Process submitted documents and apply their digital signatures.':
    'Xử lý chứng từ đã trình và áp dụng chữ ký số.',
  'Approve and sign': 'Phê duyệt và ký',
  Refresh: 'Làm mới',
  'Manual execution': 'Thực thi thủ công',
  'Run outbox poller': 'Chạy Outbox poller',
  'Fetch pending T24R records and process the signing pipeline.':
    'Lấy bản ghi T24R đang chờ và xử lý luồng ký số.',
  'Business date': 'Ngày nghiệp vụ',
  'Record limit': 'Giới hạn bản ghi',
  'Run pipeline': 'Chạy luồng xử lý',
  'Total events': 'Tổng sự kiện',
  'Recent events': 'Sự kiện gần đây',
  'Latest outbox delivery attempts': 'Các lần chuyển Outbox gần nhất',
  Event: 'Sự kiện',
  Transaction: 'Giao dịch',
  Retries: 'Số lần thử lại',
  Created: 'Ngày tạo',
  'Review security, downloads and operational actions.':
    'Xem hoạt động bảo mật, tải xuống và vận hành.',
  Time: 'Thời gian',
  Action: 'Hành động',
  Actor: 'Người thực hiện',
  Target: 'Đối tượng',
  Description: 'Mô tả',
  'No audit entries found.': 'Không tìm thấy nhật ký kiểm toán.',
  'Control access to administrative and document functions.':
    'Kiểm soát quyền truy cập chức năng quản trị và chứng từ.',
  accounts: 'tài khoản',
  'Create account': 'Tạo tài khoản',
  'Passwords require at least 8 characters': 'Mật khẩu phải có ít nhất 8 ký tự',
  'Display name': 'Tên hiển thị',
  'Full name': 'Họ và tên',
  'Temporary password': 'Mật khẩu tạm thời',
  'Access role': 'Vai trò truy cập',
  'Current accounts': 'Danh sách tài khoản',
  'Manage access status': 'Quản lý trạng thái truy cập',
  User: 'Người dùng',
  Role: 'Vai trò',
  Disable: 'Vô hiệu hóa',
  Enable: 'Kích hoạt',
  ACTIVE: 'HOẠT ĐỘNG',
  DISABLED: 'VÔ HIỆU',
  'Dark mode': 'Chế độ tối',
  'Light mode': 'Chế độ sáng',
};

@Injectable({ providedIn: 'root' })
export class PreferencesService {
  readonly language = signal<Language>(
    localStorage.getItem('t24r-language') === 'vi' ? 'vi' : 'en',
  );
  readonly darkMode = signal(localStorage.getItem('t24r-theme') === 'dark');

  setLanguage(language: Language): void {
    this.language.set(language);
    localStorage.setItem('t24r-language', language);
  }

  toggleTheme(): void {
    this.darkMode.update((darkMode) => !darkMode);
    localStorage.setItem('t24r-theme', this.darkMode() ? 'dark' : 'light');
  }

  t(text: string): string {
    return this.language() === 'vi' ? (VI[text] ?? text) : text;
  }
}
