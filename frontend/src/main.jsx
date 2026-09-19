import React, { useEffect, useMemo, useState } from 'react';
import { createRoot } from 'react-dom/client';
import { api } from './api';
import './styles.css';

const statusLabel = (status) => status?.replaceAll('_', ' ');
const money = (value) => new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(value || 0);
const dateTime = (value) => value ? new Date(value).toLocaleString('en-IN', { dateStyle: 'medium', timeStyle: 'short' }) : '—';
const mask = (name = '') => name.split(' ').map(part => part ? `${part[0]}${'•'.repeat(Math.max(part.length - 1, 2))}` : '').join(' ');

function App() {
  const [alerts, setAlerts] = useState([]);
  const [accounts, setAccounts] = useState([]);
  const [transactions, setTransactions] = useState([]);
  const [selected, setSelected] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [activeView, setActiveView] = useState('queue');
  const [filter, setFilter] = useState('ALL');

  const load = async () => {
    setLoading(true); setError('');
    try {
      const [alertData, accountData, transactionData] = await Promise.all([api.alerts(), api.accounts(), api.transactions()]);
      setAlerts(alertData); setAccounts(accountData); setTransactions(transactionData);
      setSelected(current => current ? alertData.find(item => item.id === current.id) || null : alertData[0] || null);
    } catch (e) { setError(`${e.message}. Ensure the Spring Boot backend is running on port 8080.`); }
    finally { setLoading(false); }
  };
  useEffect(() => { load(); }, []);
  const filtered = useMemo(() => filter === 'ALL' ? alerts : alerts.filter(a => a.status === filter), [alerts, filter]);
  const openAlerts = alerts.filter(a => ['OPEN', 'UNDER_REVIEW', 'ESCALATED'].includes(a.status));
  const selectedAccountTransactions = selected ? transactions.filter(t => t.account?.id === selected.account?.id).sort((a,b) => new Date(b.transactionTime) - new Date(a.transactionTime)) : [];

  return <main>
    <header className="topbar"><div><span className="eyebrow">MERIDIANTRUST BANK</span><h1>Sentinel <em>AML</em></h1></div><div className="live"><i/> Monitoring live <button onClick={load}>Refresh</button></div></header>
    <section className="stats">
      <Metric label="Open alerts" value={openAlerts.length} detail="Needs analyst attention" tone="red" />
      <Metric label="High risk" value={alerts.filter(a => a.riskScore >= 80).length} detail="Score of 80 or higher" tone="orange" />
      <Metric label="Transactions" value={transactions.length} detail="Received by Sentinel" tone="blue" />
      <Metric label="Customers monitored" value={new Set(accounts.map(a => a.customer?.id)).size} detail="Across imported accounts" tone="green" />
    </section>
    <nav><button className={activeView === 'queue' ? 'active' : ''} onClick={() => setActiveView('queue')}>Alert queue</button><button className={activeView === 'intake' ? 'active' : ''} onClick={() => setActiveView('intake')}>Transaction intake</button></nav>
    {error && <div className="error">{error}</div>}
    {loading ? <p className="loading">Loading Sentinel data…</p> : activeView === 'intake' ? <Intake accounts={accounts} onIngest={async payload => { await api.ingest(payload); await load(); setActiveView('queue'); }} /> : <section className="workspace">
      <div className="left-column">
        <section className="panel queue"><div className="panel-header"><div><span className="eyebrow">PRIORITY WORKLIST</span><h2>Alert queue</h2></div><select value={filter} onChange={e => setFilter(e.target.value)}><option value="ALL">All statuses</option><option value="OPEN">Open</option><option value="UNDER_REVIEW">Under review</option><option value="ESCALATED">Escalated</option><option value="REPORTED">Reported</option><option value="CLOSED_FALSE_POSITIVE">Closed</option></select></div>
          {filtered.length ? <div className="alert-list">{filtered.map(alert => <button key={alert.id} className={`alert-row ${selected?.id === alert.id ? 'selected' : ''}`} onClick={() => setSelected(alert)}><Risk score={alert.riskScore}/><div className="alert-copy"><strong>{alert.ruleName.replaceAll('_', ' ')}</strong><span>{mask(alert.customer?.fullName)} · {alert.account?.accountNumber}</span><small>{dateTime(alert.createdAt)}</small></div><Status status={alert.status}/></button>)}</div> : <Empty text="No alerts match this filter." />}
        </section>
        <Heatmap alerts={alerts}/>
      </div>
      <section className="panel detail">{selected ? <CaseDetail alert={selected} transactions={selectedAccountTransactions} onSaved={load}/> : <Empty text="Select an alert to start an investigation."/>}</section>
    </section>}
  </main>;
}

function Metric({label, value, detail, tone}) { return <article className={`metric ${tone}`}><span>{label}</span><strong>{value}</strong><small>{detail}</small></article>; }
function Risk({score}) { return <div className={`risk ${score >= 80 ? 'critical' : score >= 60 ? 'high' : 'medium'}`}><b>{score}</b><small>risk</small></div>; }
function Status({status}) { return <span className={`status ${status?.toLowerCase()}`}>{statusLabel(status)}</span>; }
function Empty({text}) { return <div className="empty">{text}</div>; }

function Heatmap({alerts}) { const bands = [{label:'Critical', range:'80–100', color:'critical', count:alerts.filter(a=>a.riskScore>=80).length},{label:'High',range:'60–79',color:'high',count:alerts.filter(a=>a.riskScore>=60&&a.riskScore<80).length},{label:'Medium',range:'0–59',color:'medium',count:alerts.filter(a=>a.riskScore<60).length}]; return <section className="panel heatmap"><div className="panel-header"><div><span className="eyebrow">RISK DISTRIBUTION</span><h2>Risk heatmap</h2></div></div><div className="heat-grid">{bands.map(b=><div className={`heat ${b.color}`} key={b.label}><span>{b.label}</span><strong>{b.count}</strong><small>{b.range}</small></div>)}</div></section>; }

function CaseDetail({alert, transactions, onSaved}) {
  const [status, setStatus] = useState(alert.status); const [analystName, setAnalystName] = useState(alert.analystName || ''); const [reason, setReason] = useState(alert.dispositionReason || ''); const [message, setMessage] = useState('');
  useEffect(() => { setStatus(alert.status); setAnalystName(alert.analystName || ''); setReason(alert.dispositionReason || ''); setMessage(''); }, [alert]);
  const save = async e => { e.preventDefault(); try { await api.dispose(alert.id,{status,analystName,dispositionReason:reason}); setMessage('Case disposition saved.'); await onSaved(); } catch (err) { setMessage(err.message); } };
  return <><div className="case-heading"><div><span className="eyebrow">CASE / ALERT #{alert.id}</span><h2>{alert.ruleName.replaceAll('_', ' ')}</h2><p>{alert.explanation}</p></div><Risk score={alert.riskScore}/></div><div className="subject"><span className="avatar">{alert.customer?.fullName?.[0]}</span><div><strong>{alert.customer?.fullName}</strong><span>Customer reference visible only in case detail</span></div><div><small>ACCOUNT</small><strong>{alert.account?.accountNumber}</strong></div></div><section className="timeline"><div className="section-title"><h3>Transaction timeline</h3><span>{transactions.length} records</span></div>{transactions.length ? transactions.map(tx => <div className="event" key={tx.id}><span className={`dot ${tx.transactionType?.includes('OUT') || tx.transactionType === 'WITHDRAWAL' ? 'out' : ''}`}/><div><strong>{tx.transactionType?.replaceAll('_',' ')}</strong><span>{tx.counterparty || 'No counterparty'} · {tx.jurisdiction || 'Domestic'}</span></div><div className="event-value"><strong>{money(tx.amountInInr)}</strong><span>{dateTime(tx.transactionTime)}</span></div></div>) : <Empty text="No transaction evidence has been ingested yet."/>}</section><form className="disposition" onSubmit={save}><div className="section-title"><h3>Case disposition</h3><Status status={status}/></div><div className="form-grid"><label>New status<select value={status} onChange={e=>setStatus(e.target.value)}>{['OPEN','UNDER_REVIEW','ESCALATED','CLOSED_FALSE_POSITIVE','REPORTED'].map(x=><option key={x}>{x}</option>)}</select></label><label>Analyst name<input required value={analystName} onChange={e=>setAnalystName(e.target.value)} placeholder="e.g. Riya Patel"/></label></div><label>Disposition reason<textarea required value={reason} onChange={e=>setReason(e.target.value)} placeholder="Record the investigation outcome and rationale."/></label><button className="primary">Save case decision</button>{message && <span className="form-message">{message}</span>}</form></>;
}

function Intake({accounts, onIngest}) { const [form,setForm]=useState({accountId:accounts[0]?.id || '',transactionType:'DEPOSIT',amount:'800000',currency:'INR',transactionTime:new Date().toISOString().slice(0,16),counterparty:'',jurisdiction:'IN',channel:'BRANCH'}); const [message,setMessage]=useState(''); useEffect(()=>setForm(f=>({...f,accountId:f.accountId||accounts[0]?.id||''})),[accounts]); const submit=async e=>{e.preventDefault();try{await onIngest({...form,accountId:Number(form.accountId),amount:Number(form.amount),transactionTime:form.transactionTime+':00'});setMessage('Transaction ingested and AML rules evaluated.');}catch(err){setMessage(err.message);}}; return <section className="panel intake"><span className="eyebrow">REAL-TIME SIMULATOR</span><h2>Ingest a transaction</h2><p>Submit a transaction to store it, evaluate AML rules, and create any required alert.</p><form onSubmit={submit}><div className="form-grid"><label>Account<select value={form.accountId} onChange={e=>setForm({...form,accountId:e.target.value})}>{accounts.map(a=><option value={a.id} key={a.id}>{a.accountNumber} — {a.customer?.fullName}</option>)}</select></label><label>Transaction type<select value={form.transactionType} onChange={e=>setForm({...form,transactionType:e.target.value})}>{['DEPOSIT','WITHDRAWAL','TRANSFER_IN','TRANSFER_OUT'].map(x=><option key={x}>{x}</option>)}</select></label><label>Amount<input type="number" min="1" required value={form.amount} onChange={e=>setForm({...form,amount:e.target.value})}/></label><label>Currency<select value={form.currency} onChange={e=>setForm({...form,currency:e.target.value})}>{['INR','USD','EUR'].map(x=><option key={x}>{x}</option>)}</select></label><label>Time<input type="datetime-local" required value={form.transactionTime} onChange={e=>setForm({...form,transactionTime:e.target.value})}/></label><label>Channel<input value={form.channel} onChange={e=>setForm({...form,channel:e.target.value})}/></label><label>Counterparty<input value={form.counterparty} onChange={e=>setForm({...form,counterparty:e.target.value})} placeholder="Optional"/></label><label>Jurisdiction<input value={form.jurisdiction} onChange={e=>setForm({...form,jurisdiction:e.target.value})} placeholder="IN, IR, KP…"/></label></div><button className="primary">Ingest and evaluate</button>{message && <span className="form-message">{message}</span>}</form></section>; }

createRoot(document.getElementById('root')).render(<App/>);
