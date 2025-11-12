/**
 * 打印订单
 */
export function printOrder(order) {
  const printWindow = window.open('', '_blank', 'width=800,height=600')
  
  if (!printWindow) {
    throw new Error('无法打开打印窗口，请检查浏览器弹窗设置')
  }
  
  const html = `
    <!DOCTYPE html>
    <html>
    <head>
      <meta charset="UTF-8">
      <title>订单打印 - ${order.orderNo}</title>
      <style>
        * {
          margin: 0;
          padding: 0;
          box-sizing: border-box;
        }
        body {
          font-family: Arial, "Microsoft YaHei", sans-serif;
          padding: 30px;
          font-size: 14px;
        }
        .header {
          text-align: center;
          margin-bottom: 30px;
          border-bottom: 2px solid #000;
          padding-bottom: 10px;
        }
        .header h1 {
          font-size: 24px;
          margin-bottom: 5px;
        }
        .info {
          margin-bottom: 20px;
          line-height: 1.8;
        }
        .info-row {
          display: flex;
          margin: 5px 0;
        }
        .info-row label {
          font-weight: bold;
          width: 100px;
        }
        table {
          width: 100%;
          border-collapse: collapse;
          margin: 20px 0;
        }
        th, td {
          border: 1px solid #000;
          padding: 10px;
          text-align: left;
        }
        th {
          background-color: #f0f0f0;
          font-weight: bold;
        }
        .text-right {
          text-align: right;
        }
        .text-center {
          text-align: center;
        }
        .total {
          text-align: right;
          margin-top: 20px;
          font-size: 18px;
        }
        .total strong {
          font-size: 20px;
          color: #000;
        }
        .footer {
          margin-top: 40px;
          padding-top: 20px;
          border-top: 1px solid #ccc;
          font-size: 12px;
          color: #666;
          text-align: center;
        }
        @media print {
          body {
            margin: 0;
            padding: 20px;
          }
          .no-print {
            display: none;
          }
        }
      </style>
    </head>
    <body>
      <div class="header">
        <h1>销售订单</h1>
        <div>订单编号：${order.orderNo}</div>
      </div>
      
      <div class="info">
        <div class="info-row">
          <label>客户名称：</label>
          <span>${order.customerName || '-'}</span>
        </div>
        <div class="info-row">
          <label>订单日期：</label>
          <span>${order.createTime ? new Date(order.createTime).toLocaleString('zh-CN') : '-'}</span>
        </div>
        <div class="info-row">
          <label>订单状态：</label>
          <span>${getStatusText(order.status)}</span>
        </div>
        ${order.remark ? `
        <div class="info-row">
          <label>备注：</label>
          <span>${order.remark}</span>
        </div>
        ` : ''}
      </div>
      
      <table>
        <thead>
          <tr>
            <th class="text-center" width="50">序号</th>
            <th>商品名称</th>
            <th class="text-right" width="100">单价</th>
            <th class="text-center" width="80">数量</th>
            <th class="text-right" width="120">小计</th>
          </tr>
        </thead>
        <tbody>
          ${(order.items || []).map((item, index) => `
            <tr>
              <td class="text-center">${index + 1}</td>
              <td>${item.productName || '-'}</td>
              <td class="text-right">¥${(item.price || 0).toFixed(2)}</td>
              <td class="text-center">${item.quantity || 0}</td>
              <td class="text-right">¥${(item.subtotal || 0).toFixed(2)}</td>
            </tr>
          `).join('')}
        </tbody>
      </table>
      
      <div class="total">
        订单总额：<strong>¥${(order.totalAmount || 0).toFixed(2)}</strong>
      </div>
      
      <div class="footer">
        打印时间：${new Date().toLocaleString('zh-CN')}
      </div>
      
      <script>
        window.onload = function() {
          // 延迟打印，确保内容加载完成
          setTimeout(() => {
            window.print()
            // 打印完成后可选择关闭窗口
            // window.onafterprint = function() { window.close() }
          }, 500)
        }
      </script>
    </body>
    </html>
  `
  
  printWindow.document.write(html)
  printWindow.document.close()
}

/**
 * 打印对账单
 */
export function printStatement(statement) {
  const printWindow = window.open('', '_blank', 'width=900,height=700')
  
  if (!printWindow) {
    throw new Error('无法打开打印窗口，请检查浏览器弹窗设置')
  }
  
  const html = `
    <!DOCTYPE html>
    <html>
    <head>
      <meta charset="UTF-8">
      <title>对账单 - ${statement.customerName}</title>
      <style>
        * {
          margin: 0;
          padding: 0;
          box-sizing: border-box;
        }
        body {
          font-family: Arial, "Microsoft YaHei", sans-serif;
          padding: 30px;
          font-size: 14px;
        }
        .header {
          text-align: center;
          margin-bottom: 30px;
          border-bottom: 2px solid #000;
          padding-bottom: 10px;
        }
        .header h1 {
          font-size: 24px;
          margin-bottom: 5px;
        }
        .info {
          margin-bottom: 20px;
          line-height: 1.8;
        }
        .info-row {
          display: flex;
          margin: 5px 0;
        }
        .info-row label {
          font-weight: bold;
          width: 120px;
        }
        .summary {
          background: #f5f5f5;
          padding: 15px;
          margin: 20px 0;
          border: 1px solid #ddd;
          border-radius: 4px;
        }
        .summary-row {
          display: flex;
          justify-content: space-between;
          margin: 8px 0;
          font-size: 15px;
        }
        .summary-row.highlight {
          font-size: 18px;
          font-weight: bold;
          color: #000;
          border-top: 1px solid #ccc;
          padding-top: 10px;
          margin-top: 10px;
        }
        table {
          width: 100%;
          border-collapse: collapse;
          margin: 20px 0;
        }
        th, td {
          border: 1px solid #000;
          padding: 10px 8px;
          text-align: left;
        }
        th {
          background-color: #e0e0e0;
          font-weight: bold;
        }
        .text-right {
          text-align: right;
        }
        .text-center {
          text-align: center;
        }
        .type-sale {
          color: #d32f2f;
          font-weight: bold;
        }
        .type-payment {
          color: #388e3c;
          font-weight: bold;
        }
        .footer {
          margin-top: 40px;
          padding-top: 20px;
          border-top: 1px solid #ccc;
          font-size: 12px;
          color: #666;
          text-align: center;
        }
        @media print {
          body {
            margin: 0;
            padding: 20px;
          }
          .no-print {
            display: none;
          }
        }
      </style>
    </head>
    <body>
      <div class="header">
        <h1>客户对账单</h1>
      </div>
      
      <div class="info">
        <div class="info-row">
          <label>客户名称：</label>
          <span>${statement.customerName || '-'}</span>
        </div>
        <div class="info-row">
          <label>对账期间：</label>
          <span>${statement.startDate || '-'} 至 ${statement.endDate || '-'}</span>
        </div>
      </div>
      
      <div class="summary">
        <div class="summary-row">
          <span>期初余额：</span>
          <span>¥${(statement.openingBalance || 0).toFixed(2)}</span>
        </div>
        <div class="summary-row">
          <span>本期销售：</span>
          <span style="color: #d32f2f;">¥${(statement.periodSales || 0).toFixed(2)}</span>
        </div>
        <div class="summary-row">
          <span>本期收款：</span>
          <span style="color: #388e3c;">¥${(statement.periodPayments || 0).toFixed(2)}</span>
        </div>
        <div class="summary-row highlight">
          <span>期末余额：</span>
          <span>¥${(statement.closingBalance || 0).toFixed(2)}</span>
        </div>
      </div>
      
      <h3 style="margin: 20px 0 10px 0;">对账明细</h3>
      
      <table>
        <thead>
          <tr>
            <th class="text-center" width="100">日期</th>
            <th class="text-center" width="80">类型</th>
            <th width="150">单号</th>
            <th class="text-right" width="100">应收</th>
            <th class="text-right" width="100">实收</th>
            <th class="text-right" width="120">余额</th>
          </tr>
        </thead>
        <tbody>
          ${(statement.details || []).map(detail => `
            <tr>
              <td class="text-center">${detail.date || '-'}</td>
              <td class="text-center ${detail.type === '销售' ? 'type-sale' : 'type-payment'}">
                ${detail.type || '-'}
              </td>
              <td>${detail.refNo || '-'}</td>
              <td class="text-right">
                ${detail.debit > 0 ? '¥' + detail.debit.toFixed(2) : '-'}
              </td>
              <td class="text-right">
                ${detail.credit > 0 ? '¥' + detail.credit.toFixed(2) : '-'}
              </td>
              <td class="text-right" style="font-weight: bold;">
                ¥${(detail.balance || 0).toFixed(2)}
              </td>
            </tr>
          `).join('')}
        </tbody>
      </table>
      
      <div class="footer">
        打印时间：${new Date().toLocaleString('zh-CN')}
      </div>
      
      <script>
        window.onload = function() {
          setTimeout(() => {
            window.print()
          }, 500)
        }
      </script>
    </body>
    </html>
  `
  
  printWindow.document.write(html)
  printWindow.document.close()
}

// 辅助函数：获取状态文本
function getStatusText(status) {
  const statusMap = {
    'PENDING': '待审核',
    'APPROVED': '已审核',
    'SHIPPED': '已发货',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return statusMap[status] || status
}
