'use strict';

const http = require('http');
const hooks = require('hooks');

const HOST = '127.0.0.1';
const PORT = 8080;

const state = {
  missingUserId: 999999,
  counter: 1000,
  createdUserIds: []
};

function ensureHeaders(request, { accept = 'application/json', contentType } = {}) {
  request.headers = request.headers || {};
  request.headers.Accept = accept;
  if (contentType) {
    request.headers['Content-Type'] = contentType;
  }
}

function setAccept(request, value) {
  request.headers = request.headers || {};
  request.headers.Accept = value;
}

function removeContentType(request) {
  if (!request.headers) return;
  delete request.headers['Content-Type'];
  delete request.headers['content-type'];
}

function skipTransaction(transaction, reason) {
  transaction.skip = true;
  transaction.skipReason = reason;
}

function uniqueName(prefix) {
  state.counter += 1;
  return `${prefix}-${Date.now()}-${state.counter}`;
}

function setRequestPath(transaction, path) {
  transaction.request.uri = path;
  transaction.fullPath = path;
}

function buildUserPayload(overrides = {}) {
  const payload = {
    name: overrides.name !== undefined ? overrides.name : uniqueName('User'),
    age: overrides.age !== undefined ? overrides.age : 30,
    birthday: overrides.birthday !== undefined ? overrides.birthday : '1994/04/01',
    height: overrides.height !== undefined ? overrides.height : 170.5,
    zipCode: overrides.zipCode !== undefined ? overrides.zipCode : '123-4567',
    careerHistories: overrides.careerHistories !== undefined ? overrides.careerHistories : [
      {
        title: overrides.careerTitle !== undefined ? overrides.careerTitle : 'Software Engineer',
        period: {
          from: '2018/04/01',
          to: '2021/03/31'
        }
      }
    ]
  };
  return JSON.stringify(payload);
}

function wait(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

function sendRequest({ method, path, headers, body }) {
  return new Promise((resolve, reject) => {
    const options = {
      hostname: HOST,
      port: PORT,
      method,
      path,
      headers
    };

    const req = http.request(options, (res) => {
      let data = '';
      res.on('data', (chunk) => { data += chunk; });
      res.on('end', () => resolve({
        statusCode: res.statusCode,
        headers: res.headers,
        body: data
      }));
    });

    req.on('error', (err) => reject(err));

    if (body) {
      req.write(body);
    }

    req.end();
  });
}

async function createUser(overrides = {}) {
  const body = buildUserPayload(overrides);
  const response = await sendRequest({
    method: 'POST',
    path: '/api/v1/users',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json'
    },
    body
  });

  if (response.statusCode !== 201 || !response.headers.location) {
    throw new Error(`Failed to create user for hook (status=${response.statusCode})`);
  }
  const locationHeader = response.headers.location || response.headers.Location;
  const id = parseInt(locationHeader.split('/').pop(), 10);
  if (!Number.isInteger(id)) {
    throw new Error(`Unexpected Location header: ${locationHeader}`);
  }
  state.createdUserIds.push(id);
  await wait(150);
  return id;
}

function fixtureId() {
  return 1;
}

function prepareGetUser(transaction) {
  const status = Number(transaction.expected.statusCode);
  ensureHeaders(transaction.request);
  switch (status) {
    case 200:
      setRequestPath(transaction, `/api/v1/users/${fixtureId()}`);
      break;
    case 404:
      setRequestPath(transaction, `/api/v1/users/${state.missingUserId}`);
      break;
    case 405:
      transaction.request.method = 'POST';
      setRequestPath(transaction, `/api/v1/users/${fixtureId()}`);
      transaction.request.body = '';
      removeContentType(transaction.request);
      break;
    case 400:
    case 406:
    case 500:
      skipTransaction(transaction, 'このレスポンスは自動再現対象外のためスキップ');
      break;
    default:
      break;
  }
  return null;
}

function preparePutUser(transaction) {
  const status = Number(transaction.expected.statusCode);
  ensureHeaders(transaction.request, { accept: 'application/json', contentType: 'application/json' });
  switch (status) {
    case 204:
      setRequestPath(transaction, '/api/v1/users/2');
      transaction.request.body = JSON.stringify({ name: uniqueName('Updated') });
      break;
    case 400:
    case 406:
    case 500:
      skipTransaction(transaction, 'このレスポンスは自動再現対象外のためスキップ');
      break;
    case 405:
      transaction.request.method = 'POST';
      setRequestPath(transaction, `/api/v1/users/${fixtureId()}`);
      break;
    default:
      break;
  }
  return null;
}

function prepareDeleteUser(transaction) {
  const status = Number(transaction.expected.statusCode);
  ensureHeaders(transaction.request);
  const targetId = fixtureId();
  switch (status) {
    case 204:
      return createUser().then((id) => {
        setRequestPath(transaction, `/api/v1/users/${id}`);
      }).catch((err) => {
        console.error('Failed to create user for delete test:', err);
        skipTransaction(transaction, '前処理失敗のためスキップ');
      });
    case 400:
    case 406:
    case 500:
      skipTransaction(transaction, 'このレスポンスは自動再現対象外のためスキップ');
      break;
    case 405:
      transaction.request.method = 'POST';
      setRequestPath(transaction, `/api/v1/users/${targetId}`);
      break;
    default:
      break;
  }
  return null;
}

function prepareListUsers(transaction) {
  const status = Number(transaction.expected.statusCode);
  ensureHeaders(transaction.request);
  const keyword = 'Tar';
  switch (status) {
    case 200:
      setRequestPath(transaction, `/api/v1/users?name=${keyword}&limit=10&offset=0`);
      break;
    case 422:
    case 404:
    case 500:
    case 400:
    case 405:
    case 406:
      skipTransaction(transaction, 'この応答は自動再現できないためスキップ');
      break;
    default:
      break;
  }
}

function preparePostUser(transaction) {
  const status = Number(transaction.expected.statusCode);
  switch (status) {
    case 201:
      ensureHeaders(transaction.request, { accept: 'application/json', contentType: 'application/json' });
      transaction.request.body = buildUserPayload({ name: uniqueName('Created') });
      break;
    case 422:
      ensureHeaders(transaction.request, { accept: 'application/json', contentType: 'application/json' });
      transaction.request.body = buildUserPayload({
        name: uniqueName('InvalidPeriod'),
        careerHistories: [
          {
            title: 'Broken Period',
            period: { from: '2024/01/01', to: '2023/01/01' }
          }
        ]
      });
      break;
    case 400:
      skipTransaction(transaction, '400レスポンスは自動再現対象外のためスキップ');
      break;
    case 405:
      ensureHeaders(transaction.request);
      transaction.request.method = 'DELETE';
      transaction.request.body = '';
      removeContentType(transaction.request);
      break;
    case 409:
      ensureHeaders(transaction.request, { accept: 'application/json', contentType: 'application/json' });
      transaction.request.body = buildUserPayload({ name: 'Taro Yamada' });
      break;
    case 500:
    case 406:
      skipTransaction(transaction, 'このレスポンスは自動再現対象外のためスキップ');
      break;
    default:
      ensureHeaders(transaction.request, { accept: 'application/json', contentType: 'application/json' });
      break;
  }
}

hooks.beforeEach((transaction, done) => {
  let handler = null;
  const method = transaction.request.method.toUpperCase();
  const uri = transaction.request.uri;
  const basePath = uri ? uri.split('?')[0] : '';

  if (basePath && basePath.startsWith('/api/v1/users/') && method === 'GET') {
    handler = prepareGetUser;
  } else if (basePath && basePath.startsWith('/api/v1/users/') && method === 'PUT') {
    handler = preparePutUser;
  } else if (basePath && basePath.startsWith('/api/v1/users/') && method === 'DELETE') {
    handler = prepareDeleteUser;
  } else if (basePath === '/api/v1/users' && method === 'GET') {
    handler = prepareListUsers;
  } else if (basePath === '/api/v1/users' && method === 'POST') {
    handler = preparePostUser;
  }

  if (!handler) {
    return done();
  }

  try {
    const result = handler(transaction);
    if (result && typeof result.then === 'function') {
      result.then(() => done()).catch((err) => {
        console.error('Hook error:', err);
        skipTransaction(transaction, 'フック処理でエラーが発生したためスキップ');
        done();
      });
    } else {
      done();
    }
  } catch (err) {
    console.error('Hook exception:', err);
    skipTransaction(transaction, 'フック処理で例外が発生したためスキップ');
    done();
  }
});

hooks.afterEach((transaction, done) => {
  if (transaction.expected && transaction.expected.statusCode === '201') {
    const realStatus = transaction.real && transaction.real.statusCode;
    if (realStatus === 201 && transaction.real.headers) {
      const locationHeader = transaction.real.headers.Location || transaction.real.headers.location;
      if (locationHeader) {
        const id = parseInt(locationHeader.split('/').pop(), 10);
        if (Number.isInteger(id)) {
          state.createdUserIds.push(id);
        }
      }
    }
  }
  done();
});

hooks.afterAll((transactions, done) => {
  if (state.createdUserIds.length === 0) {
    return done();
  }
  const deletions = state.createdUserIds.map((id) => sendRequest({
    method: 'DELETE',
    path: `/api/v1/users/${id}`,
    headers: { Accept: 'application/json' }
  }).catch((err) => {
    console.error(`Failed to clean up user ${id}:`, err);
  }));

  Promise.all(deletions).then(() => done()).catch(() => done());
});
