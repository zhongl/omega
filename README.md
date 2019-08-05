## Domain Object

### Definition View

```yaml
hourly: 
  echo: collect data by hour
  only:
    cron: 0 * * * * *
    
daily:
  echo: filter
  only:
    task: 
      hourly: 24
    cron: 0 0 * * *
```


```plantuml
interface Task<C, P> {
    name: String
    description: String
    command: C
    precondition: P
}

interface Scheduler<C, P> {
    tasks: Collection<Task<C, P>>    
    plan(task: Task<C, P>): Optional<Task<C, P>> 
    pause(task: Task<C, P>): Optional<Task<C, P>> 
    remove(task: Task<C, P>): Optional<Task<C, P>> 
    latest(task: Task<C, P>): Execution<C, P>
    history(task: Task<C, P>): List<Execution<C, P>>
}

interface DryRun<C, P> {
    execute(task: Task<C, P>): Execution<C, P>
}

interface Execution<C, P> {
    state: State
    task: Task<C, P>
    journals: List<Journal> 
    cancel(force: Boolean): void
}

interface Journal {
    timestamp: long
    message: String
}

```

## Dry run a task


```plantuml
actor User as U
participant Task as T
participant DryRun as D
participant Execution as E


create T
U -> T: define
U -> D: execute(task)

create E
D -> E: new
```

## Plan a task

```plantuml
actor User as U
participant Scheduler as S
participant Execution as E
participant Timer as M

U -> S: plan(task)

create E
S -> E: new

create M
S -> M: schedule(task)
M -> E: on(timeout)

E -> E: run
E -> S: finish(execution)
```

## Alarm a failure of execution

```plantuml
participant Execution as E
participant Scheduler as S
participant Alarm as A
actor User as U

E -> S: finish(failure)
S -> A: trigger(report, user)
A -> U: notify(report)
```

## Detect the reason of alarm

```plantuml
actor User as U
participant Scheduler as S
participant Execution as E

U -> S: latest(task)
U -> E: journals
```



